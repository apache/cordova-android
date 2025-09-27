<!--<!DOCTYPE html>
<html lang="hi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Class 10 GK/GS Quiz</title>
<style>
body {
  font-family: Arial, sans-serif;
  margin:0;
  padding:0;
  background:#f3f4f6;
  color:#111827;
  display:flex;
  justify-content:center;
  align-items:flex-start;
  min-height:100vh;
}
.container {
  max-width: 600px;
  width:100%;
  background:#fff;
  padding:16px;
  margin:20px;
  border-radius:10px;
  box-shadow:0 2px 6px rgba(0,0,0,0.2);
}
h1 {
  text-align:center;
  color:#ff7a59;
}
.category-select {
  margin-bottom:12px;
  display:flex;
  gap:8px;
  flex-wrap:wrap;
}
.category-select button {
  flex:1;
  padding:8px;
  border:none;
  background:#10b981;
  color:#fff;
  border-radius:6px;
  cursor:pointer;
}
.question-box {
  margin:12px 0;
}
.question {
  font-size:18px;
  margin-bottom:10px;
}
.options {
  list-style:none;
  padding:0;
}
.options li {
  background:#f9fafb;
  margin:6px 0;
  padding:10px;
  border-radius:6px;
  cursor:pointer;
  border:1px solid #e5e7eb;
}
.options li:hover { background:#ffedd5; }
.scoreboard {
  text-align:center;
  margin-top:10px;
  font-size:16px;
}
.timer {
  text-align:right;
  font-weight:bold;
  margin-top:-30px;
  margin-bottom:10px;
}
button#nextBtn {
  margin-top:10px;
  padding:8px 16px;
  background:#ff7a59;
  color:#fff;
  border:none;
  border-radius:6px;
  cursor:pointer;
}
</style>
</head>
<body>
<div class="container">
  <h1>Class 10 GK/GS Quiz</h1>
  <div class="category-select">
    <button onclick="selectCategory('History')">History</button>
    <button onclick="selectCategory('Science')">Science</button>
    <button onclick="selectCategory('Current Affairs')">Current Affairs</button>
  </div>
  <div class="timer">Time left: <span id="time">10</span>s</div>
  <div class="question-box">
    <div class="question" id="question">Select a category to start</div>
    <ul class="options" id="options"></ul>
  </div>
  <button id="nextBtn" onclick="nextQuestion()">Next Question</button>
  <div class="scoreboard">Score: <span id="score">0</span></div>
</div>

<script>
// Questions by category
const allQuestions = {
  History:[
    {question:"भारत का स्वतंत्रता दिवस कब है?", options:["15 August 1947","26 January 1950","2 October 1948","1 January 1947"], answer:"15 August 1947"},
    {question:"पहला भारतीय राष्ट्रपति कौन था?", options:["राजेंद्र प्रसाद","डॉ सर्वपल्ली राधाकृष्णन","गोलवलकर","रामनाथ कोविंद"], answer:"राजेंद्र प्रसाद"}
  ],
  Science:[
    {question:"सौरमंडल में सबसे बड़ा ग्रह कौन सा है?", options:["बुध","शनि","बृहस्पति","पृथ्वी"], answer:"बृहस्पति"},
    {question:"मानव शरीर में लाल रक्त कोशिका क्या करती है?", options:["ऑक्सीजन ले जाती है","रक्त को ठंडा करती है","सर्दी बढ़ाती है","हड्डी बनाती है"], answer:"ऑक्सीजन ले जाती है"}
  ],
  "Current Affairs":[
    {question:"वर्तमान में भारत के राष्ट्रपति कौन हैं?", options:["रामनाथ कोविंद","द्रौपदी मुर्मू","प्रणब मुखर्जी","अमित शाह"], answer:"द्रौपदी मुर्मू"},
    {question:"भारत में 2024 का आम चुनाव कब होगा?", options:["अप्रैल","मई","जून","जुलाई"], answer:"मई"}
  ]
};

let currentCategory = null;
let questions = [];
let currentIndex = 0;
let score = parseInt(localStorage.getItem('gkScore') || '0',10);
let timer = null;
let timeLeft = 10;

const questionEl = document.getElementById('question');
const optionsEl = document.getElementById('options');
const scoreEl = document.getElementById('score');
const timeEl = document.getElementById('time');

scoreEl.textContent = score;

function selectCategory(cat){
  currentCategory = cat;
  questions = [...allQuestions[cat]];
  currentIndex = 0;
  score = parseInt(localStorage.getItem('gkScore') || '0',10);
  scoreEl.textContent = score;
  loadQuestion();
}

function loadQuestion(){
  clearInterval(timer);
  if(currentIndex >= questions.length){
    alert("Quiz खत्म! आपका स्कोर: "+score);
    currentIndex = 0;
    return;
  }
  const q = questions[currentIndex];
  questionEl.textContent = q.question;
  optionsEl.innerHTML = '';
  q.options.forEach(opt=>{
    const li = document.createElement('li');
    li.textContent = opt;
    li.onclick = ()=> checkAnswer(opt);
    optionsEl.appendChild(li);
  });
  timeLeft = 10;
  timeEl.textContent = timeLeft;
  timer = setInterval(()=>{
    timeLeft--;
    timeEl.textContent = timeLeft;
    if(timeLeft<=0){
      clearInterval(timer);
      alert("Time's up! सही उत्तर: "+q.answer);
      currentIndex++;
      loadQuestion();
    }
  },1000);
}

function checkAnswer(selected){
  clearInterval(timer);
  const correct = questions[currentIndex].answer;
  if(selected === correct){
    alert("सही उत्तर ✅");
    score++;
    localStorage.setItem('gkScore', score);
    scoreEl.textContent = score;
  } else {
    alert("गलत उत्तर ❌ उत्तर: "+correct);
  }
  currentIndex++;
  loadQuestion();
}

function nextQuestion(){
  currentIndex++;
  loadQuestion();
}

</script>
</body>
</html>
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
-->

# Cordova Android

[![NPM](https://nodei.co/npm/cordova-android.png)](https://nodei.co/npm/cordova-android/)

[![Node CI](https://github.com/apache/cordova-android/workflows/Node%20CI/badge.svg?branch=master)](https://github.com/apache/cordova-android/actions?query=branch%3Amaster)
[![codecov.io](https://codecov.io/github/apache/cordova-android/coverage.svg?branch=master)](https://codecov.io/github/apache/cordova-android?branch=master)

Cordova Android is an Android application library that allows for Cordova-based projects to be built for the Android Platform. Cordova based applications are, at the core, applications written with web technology: HTML, CSS and JavaScript.

[Apache Cordova](https://cordova.apache.org/) is a project of [The Apache Software Foundation (ASF)](https://apache.org/).

## Requirements

* Java Development Kit (JDK) 17
* [Android SDK](https://developer.android.com/)
* [Node.js](https://nodejs.org)

## Create a Cordova project

Follow the instructions in the [**Create your first Cordova app**](https://cordova.apache.org/docs/en/latest/guide/cli/index.html) section of [Apache Cordova Docs](https://cordova.apache.org/docs/en/latest/)

To use a **shared framework**, for example in development, link the appropriate cordova-android platform folder path:

```bash
cordova platform add --link /path/to/cordova-android
```

## Updating a Cordova project

When you install a new version of the [`Cordova CLI`](https://www.npmjs.com/package/cordova) that pins a new version of the [`Cordova-Android`](https://www.npmjs.com/package/cordova-android) platform, you can follow these simple upgrade steps within your project:

```bash
cordova platform rm android
cordova platform add android
```

## Debugging in Android Studio

Import project in Android Studio through _File > Open_ and targeting `/path/to/your-cdv-project/platforms/android/`.

## How to Test Repo Development

```bash
npm install
npm test
```

## Further reading

* [Apache Cordova](https://cordova.apache.org/)
