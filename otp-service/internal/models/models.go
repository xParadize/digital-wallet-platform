package models

import "github.com/google/uuid"

type OtpGenerateRequest struct {
	UserID  uuid.UUID `json:"userId" binding:"required"`
	OfferID string    `json:"offerId" binding:"required"`
}

type OtpVerifyRequest struct {
	UserID  uuid.UUID `json:"userId,omitempty"`
	OfferID string    `json:"offerId,omitempty"`
	Otp     string    `json:"otp"`
}

type OtpResendRequest struct {
	UserID  uuid.UUID `json:"userId" binding:"required"`
	OfferID string    `json:"offerId" binding:"required"`
}

type OtpLinkResponse struct {
	OtpLink string `json:"otpLink"`
}

type ErrorResponse struct {
	Error   string `json:"error"`
	Code    int    `json:"code"`
	Message string `json:"message"`
}

type SuccessResponse struct {
	Success bool   `json:"success"`
	Message string `json:"message"`
}

type OtpData struct {
	Code      string `json:"code"`
	UserID    string `json:"userId"`
	OfferID   string `json:"offerId"`
	Attempts  int    `json:"attempts"`
	Resends   int    `json:"resends"`
}