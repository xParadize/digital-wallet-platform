package config

import (
	"log"
	"os"
	"path/filepath"
	"strconv"

	"github.com/joho/godotenv"
)

type Config struct {
	Port       string
	RedisAddr  string
	RedisPass  string
	RedisDB    int
	OtpTTL     int
	MaxResends int
	MaxRetries int
	JavaApiURL string
}

func NewConfig() *Config {
	loadEnvFile()

	redisHost := getEnv("OTP_REDIS_HOST", "redis")
	redisPort := getEnv("REDIS_PORT", "6379")
	redisAddr := redisHost + ":" + redisPort

	return &Config{
		Port:       getEnv("OTP_SERVICE_PORT", "8100"),
		RedisAddr:  redisAddr,
		RedisDB:    getEnvInt("REDIS_DB", 0),
		OtpTTL:     getEnvInt("OTP_DEFAULT_TTL", 60),
		MaxResends: getEnvInt("OTP_MAX_RESENDS", 3),
		MaxRetries: getEnvInt("OTP_MAX_RETRIES", 5),
		JavaApiURL: getEnv("OTP_CONFIRMATION_API_URL", "http://host.docker.internal:8006"),
	}
}

func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}

func getEnvInt(key string, defaultValue int) int {
	if value := os.Getenv(key); value != "" {
		if intValue, err := strconv.Atoi(value); err == nil {
			return intValue
		}
	}
	return defaultValue
}

func loadEnvFile() {
    envPaths := []string{
        "/app/.env",
        ".env",
        "/DigitalWallet/.env",
        "../.env",
    }

    for _, envPath := range envPaths {
        absPath, _ := filepath.Abs(envPath)
        log.Printf("Trying path: %s", absPath)

        if _, err := os.Stat(absPath); err == nil {
            err := godotenv.Load(absPath)
            if err == nil {
                log.Printf("[INFO] Env loaded from %s", absPath)
                return
            }
        }
    }

    log.Println("[WARN] No .env file found")
}
