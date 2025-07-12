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
    <title>Card Frozen Notification</title>
</head>
<body>
<div id="body">
    <div id="div">
        <p>
            Your card <span class="highlight">${card_number}</span> has been <span class="highlight">frozen</span> successfully.
        </p>
        <p>
            <strong>Frozen At:</strong> <span class="highlight">${frozen_at}</span>
        </p>
        <p>
            While frozen, your card cannot be used for purchases or transfers.<br>
            You can unfreeze it anytime via your profile.
        </p>
        <p>
            If you did not perform this action, please contact our support team immediately.
        </p>
        <div class="footer">
            This is an automated message. Please do not reply to it.<br>
            Thank you for using our service!
        </div>
    </div>
</div>
</body>
</html>
