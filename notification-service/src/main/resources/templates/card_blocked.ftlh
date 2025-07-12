<#ftl encoding="UTF-8">
<html lang="en">
<head>
    <meta charset="UTF-8">
    <style>
        #body {
            display: flex;
            flex-direction: column;
            font-family: "Segoe UI", serif;
            align-items: center;
            background-color: #607FF2;
            font-size: 20px;
            height: 100%;
        }

        #div {
            display: block;
            flex-direction: column;
            margin: 10%;
            font-family: "Segoe UI", serif;
            background-color: white;
            padding: 3%;
            border-radius: 20px;
            color: black;
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        }

        .highlight {
            font-weight: bold;
            color: #2c3e50;
        }

        .footer {
            font-size: 14px;
            color: #666;
            margin-top: 20px;
        }
    </style>
    <title>Card Blocked Notification</title>
</head>
<body>
<div id="body">
    <div id="div">
        <p>
            We would like to inform you that your card <span class="highlight">${card_number}</span> has been <span class="highlight">blocked</span> as of <span class="highlight">${blocked_at}</span>.
        </p>
        <p>
            As a result, all transactions using this card have been temporarily suspended.
        </p>
        <p>
            If you believe this was done in error, or you wish to unblock your card, please contact our support team as soon as possible.
        </p>
        <div class="footer">
            This is an automated message. Please do not reply to it.<br>
            Thank you for trusting us with your banking needs.
        </div>
    </div>
</div>
</body>
</html>
