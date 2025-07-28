package client

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"github.com/google/uuid"
)

type JavaApiClient struct {
	baseURL    string
	httpClient *http.Client
}

type OtpConfirmRequest struct {
	UserID  uuid.UUID `json:"userId"`
	OfferID string    `json:"offerId"`
}

func NewJavaApiClient(baseURL string) *JavaApiClient {
	return &JavaApiClient{
		baseURL: baseURL,
		httpClient: &http.Client{
			Timeout: 30 * time.Second,
		},
	}
}

func (c *JavaApiClient) ConfirmTransaction(userID uuid.UUID, offerID string) error {
	confirmReq := OtpConfirmRequest{
		UserID:  userID,
		OfferID: offerID,
	}

	jsonData, err := json.Marshal(confirmReq)
	if err != nil {
		return fmt.Errorf("failed to marshal request: %w", err)
	}

	url := fmt.Sprintf("%s/api/v1/transactions/confirm", c.baseURL)

	req, err := http.NewRequest("POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Accept", "application/json")

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("Java API returned status %d", resp.StatusCode)
	}

	return nil
}

func (c *JavaApiClient) SetTimeout(timeout time.Duration) {
	c.httpClient.Timeout = timeout
}