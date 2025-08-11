<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Công tắc mở Free Fire Max</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            text-align: center;
            margin-top: 100px;
        }
        .switch {
            position: relative;
            display: inline-block;
            width: 60px;
            height: 34px;
        }
        .switch input {
            display: none;
        }
        .slider {
            position: absolute;
            cursor: pointer;
            background-color: #ccc;
            transition: .4s;
            border-radius: 34px;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
        }
        .slider:before {
            position: absolute;
            content: "";
            height: 26px;
            width: 26px;
            left: 4px;
            bottom: 4px;
            background-color: white;
            border-radius: 50%;
            transition: .4s;
        }
        input:checked + .slider {
            background-color: #4CAF50;
        }
        input:checked + .slider:before {
            transform: translateX(26px);
        }
    </style>
</head>
<body>
    <h1>Công tắc mở Free Fire Max</h1>
    <label class="switch">
        <input type="checkbox" id="gameSwitch">
        <span class="slider"></span>
    </label>

    <script>
        document.getElementById('gameSwitch').addEventListener('change', function() {
            if (this.checked) {
                // Deep link để mở trực tiếp app Free Fire Max trên Android
                window.location.href = "intent://#Intent;package=com.dts.freefiremax;end";
            }
        });
    </script>
</body>
</html>
