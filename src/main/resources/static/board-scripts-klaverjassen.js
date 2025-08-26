let gameType="klaverjassen"

function showGameSpecific(gameStatus) {
    handleTrumpColorSet(gameStatus.trumpChoice)
}

function handleScoreCard(scoreModel) {
    let maxRows = 16

    let scoreList = scoreModel.scoreList
    let start = 0
    for (let i = 0; i < maxRows; i++) {
        let scoreNS = document.getElementById("pointsNS" + (i + 1))
        let scoreEW = document.getElementById("pointsEW" + (i + 1))
        let bonusNS = document.getElementById("bonusNS" + (i + 1))
        let bonusEW = document.getElementById("bonusEW" + (i + 1))
        let roundNr = document.getElementById("roundNr" + (i + 1))
        roundNr.innerHTML = "" + (i+1)
        if (scoreList.length > i) {
            scoreNS.innerHTML = scoreList[start + i].northSouthPoints
            bonusNS.innerHTML = scoreList[start + i].northSouthBonus
            scoreEW.innerHTML = scoreList[start + i].eastWestPoints
            bonusEW.innerHTML = scoreList[start + i].eastWestBonus
        } else {
            scoreNS.innerHTML = ""
            bonusNS.innerHTML = ""
            scoreEW.innerHTML = ""
            bonusEW.innerHTML = ""
        }
    }
}

//--------------------------------------------------------------------------------------------------------------

function handleGameSpecificNewRoundToBeStartedActions(gameStatus){
    clearTrumpIndicator()
    if (isHumanPlayer(gameStatus.generic.playerToMove)) {
        waitForPlayerTrumpSelection(gameStatus.generic.playerToMove)
    } else {
        setTimeout(function () {
            requestComputeTrumpCardColor(gameStatus.generic.playerToMove)
        }, 500)
    }
}

function clearTrumpIndicator() {
    document.getElementById("trumpCard").src = CardBackImage()
}

function initTrumpCardSelect(cardColor, player) {
    let cardPostFix = cardColor[0].toUpperCase() + cardColor.substring(1).toLowerCase()
    document.getElementById("trump" + cardPostFix).style.cursor = "pointer"
    document.getElementById("trump" + cardPostFix).onclick=function () {
        closeModalAndRequestTrumpSetting(cardColor, player)
    };
}

function waitForPlayerTrumpSelection(player){
    initTrumpCardSelect("CLUBS", player)
    initTrumpCardSelect("HEARTS", player)
    initTrumpCardSelect("SPADES", player)
    initTrumpCardSelect("DIAMONDS", player)
    document.getElementById("myModal").style.display = "block";
}

function closeModalAndRequestTrumpSetting(cardColor, player) {
    document.getElementById("myModal").style.display = "none";
    requestExecuteTrumpCardColorChoice(cardColor, player)
}

function handleTrumpColorSet(trumpColorModel) {
    if (trumpColorModel !== null) {
        document.getElementById("trumpCard").src = cardColorAndRankToImageURL(trumpColorModel.trumpColor, "ONE")
        clearContract()
        document.getElementById("hasContract" + playerModelToElementPostFix(trumpColorModel.contractOwner)).innerHTML = "Plays " + cardColorToSymbol(trumpColorModel.trumpColor)
    } else {
        clearContract()
    }
}

function clearContract() {
    document.getElementById("hasContractWest").innerHTML = "W"
    document.getElementById("hasContractNorth").innerHTML = "N"
    document.getElementById("hasContractEast").innerHTML = "E"
    document.getElementById("hasContractSouth").innerHTML = "S"
}

//----------------------------------------------------------------------------------------------------------------

function doSomeTest() {
    // requestPrintGame()
    run()
}

