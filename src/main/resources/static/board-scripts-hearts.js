let gameType="hearts"

function upDownSignalImage(goingUp) {
    if (goingUp)
        return "assets/Green_Arrow_Up.svg"
    else
        return "assets/Red_Arrow_Down.svg"
}

function showGameSpecific(gameStatus) {
    document.getElementById("upDownSignal").src = upDownSignalImage(gameStatus.goingUp)
}

function handleScoreCard(scoreModel) {
    let maxRows = 9
    let scoreList = scoreModel.scoreList
    let start = Math.max(0, scoreList.length - maxRows)
    for (let i = 0; i < maxRows; i++) {
        let scoreSouth = document.getElementById("scoreS" + (i + 1))
        let scoreWest = document.getElementById("scoreW" + (i + 1))
        let scoreNorth = document.getElementById("scoreN" + (i + 1))
        let scoreEast = document.getElementById("scoreE" + (i + 1))
        let roundNr = document.getElementById("roundNr" + (i + 1))
        roundNr.innerHTML = "" + (start + i + 1)
        if (scoreList.length > i) {
            scoreSouth.innerHTML = scoreList[start + i].south
            scoreWest.innerHTML = scoreList[start + i].west
            scoreEast.innerHTML = scoreList[start + i].east
            scoreNorth.innerHTML = scoreList[start + i].north
        } else {
            scoreSouth.innerHTML = ""
            scoreWest.innerHTML = ""
            scoreEast.innerHTML = ""
            scoreNorth.innerHTML = ""
        }
    }
}

function handleGameSpecificNewRoundStartActions(gameStatus){
    //do nothing
}


