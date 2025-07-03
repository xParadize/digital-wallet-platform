<#ftl encoding="UTF-8">
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <style>
        body {
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
        }

        #container {
            max-width: 600px;
            margin: 20px auto;
            padding: 20px;
            background-color: #fff;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }

        #header {
            text-align: center;
            margin-bottom: 30px;
        }

        h1 {
            color: #007bff;
        }

        p {
            font-size: 16px;
            line-height: 1.6;
            margin-bottom: 20px;
        }

        #footer {
            text-align: center;
            margin-top: 30px;
            font-size: 14px;
            color: #666;
        }

        .support-email {
            color: #666;
        }

        .alert {
            color: #d9534f;
            font-weight: bold;
        }
    </style>
</head>
<body>
<div id="container">
    <div id="header">
        <h1>Password Changed Successfully</h1>
    </div>

    <p>Hello!</p>

    <p class="alert">We've noticed that the password for your account was recently changed.</p>

    <p>If this was you, no further action is needed. If you did not request this change, please reset your password immediately and contact our support team.</p>

    <p>For your security, we recommend that you:</p>
    <ul>
        <li>Do not reuse old passwords</li>
        <li>Use a strong and unique password</li>
        <li>Enable two-factor authentication (if available)</li>
    </ul>

    <div id="footer">
        <p>If you have any questions or need assistance, feel free to contact our support team:</p>
        <p class="support-email">support@wallet.com</p>
    </div>
</div>
</body>
</html>
