package service

import (
	"crypto/rand"
	"fmt"
	"log"
	"math/big"
	"otp-service/internal/client"
	"otp-service/internal/repository"

	"github.com/google/uuid"
)

type OtpService struct {
	repo       *repository.RedisRepository
	javaClient *client.JavaApiClient
	maxRetries int
	maxResends int
}

func NewOtpService(repo *repository.RedisRepository, javaApiURL string, maxRetries int, maxResends int) *OtpService {
	return &OtpService{
		repo:       repo,
		javaClient: client.NewJavaApiClient(javaApiURL),
		maxRetries: maxRetries,
		maxResends: maxResends,
	}
}

func (s *OtpService) GenerateOtp(userID uuid.UUID, offerID string) (string, error) {
	existingOtp, err := s.repo.GetOtp(userID, offerID)
	if err == nil && existingOtp != nil {
		log.Printf("[INFO] OTP already exists | userID=%s offerID=%s | reusing existing OTP link", userID.String(), offerID)
		otpLink := fmt.Sprintf("http://localhost:8100/api/v1/otp/verify?userId=%s&offerId=%s&otp=%s",
			userID.String(), offerID, existingOtp.Code)
		return otpLink, nil
	}

	otpCode, err := s.generateOtpCode()
	if err != nil {
		log.Printf("[ERROR] OTP generation failed | userID=%s offerID=%s | error=%v", userID.String(), offerID, err)
		return "", fmt.Errorf("OTP generation failed | userID=%s offerID=%s | error=%v", userID.String(), offerID, err)
	}

	if err := s.repo.StoreOtp(userID, offerID, otpCode); err != nil {
		log.Printf("[ERROR] Redis storage failed | userID=%s offerID=%s | error=%v", userID.String(), offerID, err)
		return "", fmt.Errorf("Redis storage failed | userID=%s offerID=%s | error=%v", userID.String(), offerID, err)
	}

	otpLink := fmt.Sprintf("http://localhost:8100/api/v1/otp/verify?userId=%s&offerId=%s&otp=%s",
		userID.String(), offerID, otpCode)

	log.Printf("[INFO] New OTP generated | userID=%s offerID=%s | otp=%s", userID.String(), offerID, otpCode)
	return otpLink, nil
}

func (s *OtpService) VerifyOtp(userID uuid.UUID, offerID, otpCode string) error {
	otpData, err := s.repo.GetOtp(userID, offerID)
	if err != nil {
		log.Printf("[ERROR] Failed to retrieve OTP from Redis | userID=%s offerID=%s | error=%v", userID.String(), offerID, err)
		return fmt.Errorf("Failed to retrieve OTP from Redis | userID=%s offerID=%s | error=%v", userID.String(), offerID, err)
	}

	if otpData == nil {
		log.Printf("[WARN] No OTP found | userID=%s offerID=%s | possibly expired or deleted", userID.String(), offerID)
		return fmt.Errorf("No OTP found | userID=%s offerID=%s | possibly expired or deleted", userID.String(), offerID)
	}

	if otpData.Code != otpCode {
		log.Printf("[WARN] Incorrect OTP entered | userID=%s offerID=%s | attempt=%d", userID.String(), offerID, otpData.Attempts+1)

		newAttempts := otpData.Attempts + 1
		s.repo.UpdateOtpAttempts(userID, offerID, newAttempts)

		if newAttempts >= s.maxRetries {
			log.Printf("[WARN] OTP verification blocked | max attempts reached | userID=%s offerID=%s", userID.String(), offerID)

			s.repo.DeleteOtp(userID, offerID)

			currentResends, _ := s.repo.GetResendCount(userID, offerID)
			if currentResends >= s.maxResends {
				log.Printf("[ERROR] OTP permanently invalidated | max attempts & resends exceeded | userID=%s offerID=%s", userID.String(), offerID)
				s.repo.ResetResendCount(userID, offerID)
				return fmt.Errorf("verification failed: you have exceeded the maximum number of attempts and resends. Offer purchase is no longer available")
			}

			return fmt.Errorf("maximum attempts exceeded: you have entered the OTP code incorrectly %d times. Please request a new code using resend", s.maxRetries)
		}

		return fmt.Errorf("invalid OTP code")
	}

	err = s.javaClient.ConfirmTransaction(userID, offerID)
	if err != nil {
		log.Printf("[ERROR] Transaction confirmation failed | userID=%s offerID=%s | error=%v", userID.String(), offerID, err)
		return fmt.Errorf("Transaction confirmation failed | userID=%s offerID=%s | error=%v", userID.String(), offerID, err)
	}

	s.repo.DeleteOtp(userID, offerID)
	s.repo.ResetResendCount(userID, offerID)

	log.Printf("[INFO] Successfully verified OTP for user %s offer %s", userID.String(), offerID)
	return nil
}

func (s *OtpService) ResendOtp(userID uuid.UUID, offerID string) (string, error) {
	currentResends, err := s.repo.GetResendCount(userID, offerID)
	if err != nil {
		log.Printf("[ERROR] Failed to get resend count: %v", err)
		return "", fmt.Errorf("failed to check resend count: %w", err)
	}

	if currentResends >= s.maxResends {
		log.Printf("[WARN] Maximum resend limit exceeded for user %s offer %s", userID.String(), offerID)
		return "", fmt.Errorf("maximum resend limit exceeded: %d", s.maxResends)
	}

	_, err = s.repo.IncrementResendCount(userID, offerID)
	if err != nil {
		log.Printf("[ERROR] Failed to increment resend count: %v", err)
		return "", fmt.Errorf("failed to update resend count: %w", err)
	}

	s.repo.DeleteOtp(userID, offerID)

	otpCode, err := s.generateOtpCode()
	if err != nil {
		log.Printf("[ERROR] Failed to generate OTP code for resend: %v", err)
		return "", fmt.Errorf("failed to generate OTP code: %w", err)
	}

	if err := s.repo.StoreOtp(userID, offerID, otpCode); err != nil {
		log.Printf("[ERROR] Failed to store resent OTP: %v", err)
		return "", fmt.Errorf("failed to store OTP: %w", err)
	}

	log.Printf("[INFO] Generated new OTP for user %s offer %s: %s (resend #%d)", userID.String(), offerID, otpCode, currentResends+1)

	otpLink := fmt.Sprintf("http://localhost:8100/api/v1/otp/verify?userId=%s&offerId=%s", userID.String(), offerID)

	return otpLink, nil
}

func (s *OtpService) generateOtpCode() (string, error) {
	const otpLength = 6
	const digits = "0123456789"

	code := make([]byte, otpLength)
	for i := 0; i < otpLength; i++ {
		num, err := rand.Int(rand.Reader, big.NewInt(int64(len(digits))))
		if err != nil {
			return "", err
		}
		code[i] = digits[num.Int64()]
	}

	return string(code), nil
}