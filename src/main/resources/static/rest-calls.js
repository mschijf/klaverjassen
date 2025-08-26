let requestBase = "api/v1/" + gameType

function requestForNewGame() {
    let request = new XMLHttpRequest();

    request.open("POST", requestBase + "/new-game");
    request.onreadystatechange = function() {
        if(this.readyState === 4 && this.status === 200) {
            let gameStatus = JSON.parse(this.responseText);
            handleGameStatus(gameStatus)
        }
    };
    request.send();
}

function requestGameStatus() {
    let request = new XMLHttpRequest();

    request.open("GET", requestBase + "/game-status/");
    request.onreadystatechange = function() {
        if(this.readyState === 4 && this.status === 200) {
            let gameStatus = JSON.parse(this.responseText);
            handleGameStatus(gameStatus)
        }
    };
    request.send();
}

function requestForScoreCard() {
    let request = new XMLHttpRequest();

    request.open("GET", requestBase + "/score-list/");
    request.onreadystatechange = function() {
        if(this.readyState === 4 && this.status === 200) {
            let scoreModel = JSON.parse(this.responseText);
            handleScoreCard(scoreModel)
        }
    };
    request.send();
}


function requestComputeMove() {
    let request = new XMLHttpRequest();

    request.open("POST", requestBase + "/computeMove/");
    request.onreadystatechange = function() {
        if(this.readyState === 4 && this.status === 200) {
            let movePlayed = JSON.parse(this.responseText);
            if (movePlayed.success) {
                handleMove(movePlayed.cardPlayedModel)
            }
        }
    };
    request.send();
}

function requestDoMove(cardModel) {
    let request = new XMLHttpRequest();

    request.open("POST", requestBase + "/executeMove/" + cardModel.color + "/" + cardModel.rank);
    request.onreadystatechange = function() {
        if(this.readyState === 4 && this.status === 200) {
            let movePlayed = JSON.parse(this.responseText);
            if (movePlayed.success) {
                handleMove(movePlayed.cardPlayedModel)
            } else {
                handleIllegalMoveDone()
            }
        }
    };
    request.send();
}

function requestLog() {
    let request = new XMLHttpRequest();

    request.open("GET", requestBase + "/log/");
    request.onreadystatechange = function() {
        if(this.readyState === 4 && this.status === 200) {
            let logLines = this.responseText;
            handleLog(logLines)
        }
    };
    request.send();
}

function requestComputeTrumpCardColor(player) {
    let request = new XMLHttpRequest();

    request.open("POST", requestBase + "/computeTrumpCardChoice/" + player);
    request.onreadystatechange = function() {
        if(this.readyState === 4 && this.status === 200) {
            let gameStatus = JSON.parse(this.responseText);
            handleGameStatus(gameStatus)
        }
    };
    request.send();
}

function requestExecuteTrumpCardColorChoice(cardColor, player) {
    let request = new XMLHttpRequest();

    request.open("POST", requestBase + "/executeTrumpCardChoice/" + cardColor +"/" + player);
    request.onreadystatechange = function() {
        if(this.readyState === 4 && this.status === 200) {
            let gameStatus = JSON.parse(this.responseText);
            handleGameStatus(gameStatus)
        }
    };
    request.send();
}

function requestPrintGame() {
    let request = new XMLHttpRequest();

    request.open("POST", requestBase + "/printgame");
    request.send();
}

// Define an async function that fetches data
async function getData() {
    const response = await fetch(requestBase + "/asynctest"); // waits until response arrives
    if (!response.ok) {
        throw new Error("Network error " + response.status);
    }
    const data = await response.json(); // waits until JSON is parsed
    return data;
}

// Use it in synchronous-looking style
async function run() {
    console.log("Before AJAX call");

    let result = await getData(); // execution pauses here until result is ready

    console.log("Got result:", result);

    console.log("After AJAX call");
}

