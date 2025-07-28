package handlers

import (
	"fmt"
	"net/http"
	"net/url"
	"regexp"
	"strings"

	"otp-service/internal/models"
	"otp-service/internal/service"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type OtpHandler struct {
	otpService *service.OtpService
}

func NewOtpHandler(otpService *service.OtpService) *OtpHandler {
	return &OtpHandler{
		otpService: otpService,
	}
}

func (h *OtpHandler) GenerateOtp(c *gin.Context) {
	var req models.OtpGenerateRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "Invalid request format",
			Code:    400,
			Message: err.Error(),
		})
		return
	}

	otpLink, err := h.otpService.GenerateOtp(req.UserID, req.OfferID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, models.ErrorResponse{
			Error:   "Failed to generate OTP",
			Code:    500,
			Message: err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, models.OtpLinkResponse{
		OtpLink: otpLink,
	})
}

func (h *OtpHandler) VerifyOtp(c *gin.Context) {
	var req models.OtpVerifyRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "Invalid request format",
			Code:    400,
			Message: err.Error(),
		})
		return
	}

	if err := validateOtpCode(req.Otp); err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "Invalid OTP format",
			Code:    400,
			Message: err.Error(),
		})
		return
	}

	userID := req.UserID
	offerID := req.OfferID

	if userID == uuid.Nil || offerID == "" {
		userIDParam := c.Query("userId")
		offerIDParam := c.Query("offerId")

		if userIDParam != "" {
			parsedUserID, err := uuid.Parse(strings.TrimPrefix(userIDParam, "="))
			if err != nil {
				c.JSON(http.StatusBadRequest, models.ErrorResponse{
					Error:   "Invalid userId format",
					Code:    400,
					Message: err.Error(),
				})
				return
			}
			userID = parsedUserID
		}

		if offerIDParam != "" {
			offerID = strings.TrimPrefix(offerIDParam, "=")
		}
	}

	if userID == uuid.Nil || offerID == "" {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "Missing userId or offerId",
			Code:    400,
			Message: "Both userId and offerId are required",
		})
		return
	}

	err := h.otpService.VerifyOtp(userID, offerID, req.Otp)
	if err != nil {
		h.handleVerifyError(c, err)
		return
	}

	c.JSON(http.StatusOK, models.SuccessResponse{
		Success: true,
		Message: "OTP verified successfully and transaction confirmed",
	})
}

func (h *OtpHandler) VerifyOtpGet(c *gin.Context) {
	userIDParam := c.Query("userId")
	offerIDParam := c.Query("offerId")
	otpParam := c.Query("otp")

	if userIDParam == "" || offerIDParam == "" {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "Missing required parameters",
			Code:    400,
			Message: "userId and offerId are required query parameters",
		})
		return
	}

	userID, err := uuid.Parse(parseQueryParam(userIDParam))
	if err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "Invalid userId format",
			Code:    400,
			Message: "userId must be a valid UUID",
		})
		return
	}

	offerID := parseQueryParam(offerIDParam)
	otpCode := parseQueryParam(otpParam)

	if err := validateOtpCode(otpCode); err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "Invalid OTP format",
			Code:    400,
			Message: err.Error(),
		})
		return
	}

	err = h.otpService.VerifyOtp(userID, offerID, otpCode)
	if err != nil {
		h.handleVerifyError(c, err)
		return
	}

	c.JSON(http.StatusOK, models.SuccessResponse{
		Success: true,
		Message: "OTP verified successfully and transaction confirmed",
	})
}


func (h *OtpHandler) ResendOtp(c *gin.Context) {
	var req models.OtpResendRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "Invalid request format",
			Code:    400,
			Message: err.Error(),
		})
		return
	}

	otpLink, err := h.otpService.ResendOtp(req.UserID, req.OfferID)
	if err != nil {
		h.handleResendError(c, err)
		return
	}

	c.JSON(http.StatusOK, models.OtpLinkResponse{
		OtpLink: otpLink,
	})
}

func (h *OtpHandler) ResendOtpGet(c *gin.Context) {
	userIDParam := c.Query("userId")
	offerIDParam := c.Query("offerId")

	if userIDParam == "" || offerIDParam == "" {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "Missing required parameters",
			Code:    400,
			Message: "userId and offerId are required query parameters",
		})
		return
	}

	userID, err := uuid.Parse(parseQueryParam(userIDParam))
	if err != nil {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "Invalid userId format",
			Code:    400,
			Message: "userId must be a valid UUID",
		})
		return
	}

	offerID := parseQueryParam(offerIDParam)

	otpLink, err := h.otpService.ResendOtp(userID, offerID)
	if err != nil {
		h.handleResendError(c, err)
		return
	}

	c.JSON(http.StatusOK, models.OtpLinkResponse{
		OtpLink: otpLink,
	})
}

func (h *OtpHandler) handleVerifyError(c *gin.Context, err error) {
	errorMsg := err.Error()

	if strings.Contains(errorMsg, "invalid OTP code") {
		c.JSON(http.StatusBadRequest, models.ErrorResponse{
			Error:   "Invalid OTP code",
			Code:    400,
			Message: "The provided OTP code is incorrect",
		})
		return
	}

	if strings.Contains(errorMsg, "not found or expired") {
		c.JSON(http.StatusNotFound, models.ErrorResponse{
			Error:   "OTP not found or expired",
			Code:    404,
			Message: "The OTP code has expired or does not exist",
		})
		return
	}

	if strings.Contains(errorMsg, "maximum attempts exceeded") {
		c.JSON(http.StatusTooManyRequests, models.ErrorResponse{
			Error:   "Too many attempts",
			Code:    429,
			Message: errorMsg,
		})
		return
	}

	if strings.Contains(errorMsg, "verification failed: you have exceeded") {
		c.JSON(http.StatusForbidden, models.ErrorResponse{
			Error:   "Verification permanently failed",
			Code:    403,
			Message: errorMsg,
		})
		return
	}

	if strings.Contains(errorMsg, "failed to confirm transaction") {
		c.JSON(http.StatusServiceUnavailable, models.ErrorResponse{
			Error:   "Transaction confirmation failed",
			Code:    503,
			Message: "Unable to confirm transaction. Please try again later.",
		})
		return
	}

	c.JSON(http.StatusInternalServerError, models.ErrorResponse{
		Error:   "Verification failed",
		Code:    500,
		Message: errorMsg,
	})
}

func (h *OtpHandler) handleResendError(c *gin.Context, err error) {
	errorMsg := err.Error()

	if strings.Contains(errorMsg, "maximum resend limit exceeded") {
		c.JSON(http.StatusTooManyRequests, models.ErrorResponse{
			Error:   "Too many resend attempts",
			Code:    429,
			Message: errorMsg,
		})
		return
	}

	if strings.Contains(errorMsg, "offer not found") {
		c.JSON(http.StatusNotFound, models.ErrorResponse{
			Error:   "Offer not found",
			Code:    404,
			Message: errorMsg,
		})
		return
	}

	c.JSON(http.StatusInternalServerError, models.ErrorResponse{
		Error:   "Failed to resend OTP",
		Code:    500,
		Message: errorMsg,
	})
}

func parseQueryParam(param string) string {
	decoded, err := url.QueryUnescape(param)
	if err != nil {
		return param
	}
	return strings.TrimPrefix(decoded, "=")
}

func validateOtpCode(otp string) error {
	if otp == "" {
		return fmt.Errorf("OTP code must be exactly 6 digits")
	}

	if len(otp) != 6 {
		return fmt.Errorf("OTP code must be exactly 6 digits")
	}

	matched, err := regexp.MatchString(`^\d{6}$`, otp)
	if err != nil {
		return fmt.Errorf("error validating OTP format")
	}

	if !matched {
		return fmt.Errorf("OTP code must be exactly 6 digits")
	}

	return nil
}
