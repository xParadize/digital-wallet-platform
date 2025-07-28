package main

import (
	"log"
	"otp-service/internal/config"
	"otp-service/internal/handlers"
	"otp-service/internal/repository"
	"otp-service/internal/service"

	"github.com/gin-gonic/gin"
)

func main() {
	cfg := config.NewConfig()

	redisRepo := repository.NewRedisRepository(cfg)
	defer redisRepo.Close()

	otpService := service.NewOtpService(redisRepo, cfg.JavaApiURL, cfg.MaxRetries, cfg.MaxResends)
	otpHandler := handlers.NewOtpHandler(otpService)
	router := gin.Default()

	if gin.Mode() == gin.ReleaseMode {
		gin.DisableConsoleColor()
	}

	router.Use(gin.Recovery())

	router.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status": "ok",
			"service": "otp-service",
		})
	})

	v1 := router.Group("/api/v1/otp")
	{
		v1.POST("/generate", otpHandler.GenerateOtp)
		v1.POST("/verify", otpHandler.VerifyOtp)
		v1.GET("/verify", otpHandler.VerifyOtpGet)
		v1.POST("/resend", otpHandler.ResendOtp)
		v1.GET("/resend", otpHandler.ResendOtpGet)
	}

	log.Printf("[INFO] Starting OTP service on port: %s", cfg.Port)
	log.Printf("[INFO] Java API endpoint configured: %s", cfg.JavaApiURL)
	log.Printf("[INFO] Redis connection established: address=%s, db=%d", cfg.RedisAddr, cfg.RedisDB)
	log.Printf("[INFO] OTP TTL: %d seconds", cfg.OtpTTL)
	log.Printf("[INFO] Max resends: %d", cfg.MaxResends)
	log.Printf("[INFO] Max retries per OTP: %d", cfg.MaxRetries)

	if err := router.Run(":" + cfg.Port); err != nil {
		log.Fatal("Failed to start server:", err)
	}
}