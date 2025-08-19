window.onload = function () {
    initGame()
};

//---------------------------------------------------------------------------------------------------------------------

let __globalGameStatusGeneric = null

function NoCardImage() {
    return "carddeck/NoCard.SVG"
}

function CardBackImage() {
    return "carddeck/CardBack.SVG"
}

function cardColorToSymbol(cardColor) {
    switch (cardColor) {
        case "SPADES":
            return "&spades;"
        case "HEARTS":
            return "&hearts;"
        case "CLUBS":
            return "&clubs;"
        case "DIAMONDS":
            return "&diams;"
    }
}

function cardColorAndRankToImageURL(cardColor, cardRank) {
    let colorString = ""
    let rankString = ""
    switch (cardColor) {
        case "SPADES":
            colorString = "S";
            break;
        case "HEARTS":
            colorString = "H";
            break;
        case "CLUBS":
            colorString = "C";
            break;
        case "DIAMONDS":
            colorString = "D";
            break;
    }
    switch (cardRank) {
        case "ONE":
            rankString = "1";
            break;
        case "TWO":
            rankString = "2";
            break;
        case "THREE":
            rankString = "3";
            break;
        case "FOUR":
            rankString = "4";
            break;
        case "FIVE":
            rankString = "5";
            break;
        case "SIX":
            rankString = "6";
            break;
        case "SEVEN":
            rankString = "7";
            break;
        case "EIGHT":
            rankString = "8";
            break;
        case "NINE":
            rankString = "9";
            break;
        case "TEN":
            rankString = "10";
            break;
        case "JACK":
            rankString = "J";
            break;
        case "QUEEN":
            rankString = "Q";
            break;
        case "KING":
            rankString = "K";
            break;
        case "ACE":
            rankString = "A";
            break;
    }
    return "carddeck/" + rankString + colorString + ".SVG"
}

function cardModelToImageURL(cardModel) {
    if (cardModel == null) {
        return NoCardImage()
    }
    return cardColorAndRankToImageURL(cardModel.color, cardModel.rank)
}

function playerModelToTableImage(player) {
    return document.getElementById("table" + playerModelToElementPostFix(player))
}

function playerModelToElementPostFix(player) {
    return player[0].toUpperCase() + player.substring(1).toLowerCase()
}

function cardModelToImage(cardModel) {
    let src = cardModelToImageURL(cardModel)
    let allImages = document.getElementsByTagName('img');
    for (let i = 0; i < allImages.length; i++) {
        if (allImages[i].src.indexOf(src) >= 0) {
            return allImages[i];
        }
    }
    return null
}

function lastPlayerHandCardImage(player, cardsInHand) {
    let postFix = playerModelToElementPostFix(player)
    return document.getElementById("player" + postFix + (cardsInHand-1))
}


function isHumanPlayer(player) {
    return player === "SOUTH"
}

//-----------------------------------------------------------------------------------------

function initGame() {
    removeCardsFromTable()
    requestForNewGame()
    // requestForScoreCard()
    showInfo("")
    showJson("")
}

function showJson(msg) {
    document.getElementById("jsonText").innerHTML = msg
}

function showInfo(info) {
    document.getElementById("scoreInfo").innerHTML = info
}

function showSeed(seed) {
    document.getElementById("shuffleSeed").innerHTML = "Seed: " + seed
}

//-----------------------------------------------------------------------------------------

function showCard(cardId, cardModel, show) {
    let aCardImage = document.getElementById(cardId)
    if (cardModel != null) {
        if (show)
            aCardImage.src = cardModelToImageURL(cardModel)
        else
            aCardImage.src = CardBackImage()
    } else {
        aCardImage.src = NoCardImage()
    }
}

function showPlayerCards(player, playerHand, show) {
    for (let cardIndex = 0; cardIndex < 8; cardIndex++) {
        if (cardIndex < playerHand.length)
            showCard(player + cardIndex, playerHand[cardIndex].card, show)
        else {
            showCard(player + cardIndex, null, show)
        }
    }
}

function clearGeniusSouthValues() {
    for (let i = 0; i < 8; i++) {
        document.getElementById("meta" + i).innerHTML = ""
    }
}

function showGeniusSouthValues(cardModelListPlayerSouth) {
    for (let i = 0; i < 8; i++) {
        document.getElementById("meta" + i).innerHTML = ""
    }
    for (let i = 0; i < cardModelListPlayerSouth.length; i++) {
        document.getElementById("meta" + i).innerHTML = cardModelListPlayerSouth[i].geniusValue
    }
}

let __lastWinnerId = "pointToWinnerNorth"
function showLeader(leader) {
    let lastWinner = document.getElementById(__lastWinnerId)
    __lastWinnerId = "pointToWinner" + playerModelToElementPostFix(leader)
    lastWinner.id = __lastWinnerId
}

//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------

function handleGameStatus(gameStatus) {
    handleGameStatusGeneric(gameStatus.generic)
    showGameSpecific(gameStatus)
    requestForScoreCard();
    if (gameStatus.generic.newRoundStarted) {
        handleGameSpecificNewRoundStartActions(gameStatus)
    }
    handleNextMove(gameStatus.generic)
}

function handleGameStatusGeneric(gameStatusGeneric) {
    __globalGameStatusGeneric = gameStatusGeneric
    showPlayerCards("playerSouth", gameStatusGeneric.playerSouth, true)
    showPlayerCards("playerWest", gameStatusGeneric.playerWest, true)
    showPlayerCards("playerNorth", gameStatusGeneric.playerNorth, true)
    showPlayerCards("playerEast", gameStatusGeneric.playerEast, true)
    showExtras(gameStatusGeneric)
    showLeader(gameStatusGeneric.leadPlayer)
    showJson("")
    showSeed(gameStatusGeneric.seed)
}

function handleNextMove(gameStatusGeneric) {
    let waitForNextMove = isHumanPlayer(gameStatusGeneric.playerToMove) ? 0 : 500
    setTimeout(function () {
        doNextMove(gameStatusGeneric.playerToMove)
    }, waitForNextMove)
}


function showExtras(gameStatusGeneric) {
    clearGeniusSouthValues()
    showGeniusSouthValues(gameStatusGeneric.playerSouth)

    document.getElementById("buttonJson").onclick = function () {
        showJson(gameStatusGeneric.gameJsonString)
    };
    showJson("")
}


//-----------------------------------------------------------------------------------------

function waitForPlayerMove() {
    setClickableCards()
}

function doMove(cardModel) {
    disableClickableCards()
    requestDoMove(cardModel)
}

function setClickableCards() {
    for (let cardIndex = 0; cardIndex < __globalGameStatusGeneric.playerSouth.length; cardIndex++) {
        let aCardImage = document.getElementById("playerSouth" + cardIndex)
        let cardModel = __globalGameStatusGeneric.playerSouth[cardIndex]
        if (cardModel != null && cardModel.playable === true) {
            aCardImage.onclick = function () {
                doMove(cardModel.card)
            };
            aCardImage.style.cursor = "pointer"
        } else {
            aCardImage.onclick = null
            aCardImage.style.cursor = "default"
        }
    }
}

function disableClickableCards() {
    for (let cardIndex = 0; cardIndex < 8; cardIndex++) {
        let aCardImage = document.getElementById("playerSouth" + cardIndex)
        aCardImage.onclick = null
        aCardImage.style.cursor = "default"
    }
}


//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------

function handleIllegalMoveDone() {
    console.log("ILLEGAL CARD CHOSEN!!")
    waitForPlayerMove()
}

function handleMove(movePlayed) {
    cardFromHandToTable(movePlayed)

    let trickCompleteTime = 0
    if (movePlayed.trickCompleted != null) {
        let showWinnerTime = 2000
        let clearMoveTime = 700
        trickCompleteTime = showWinnerTime + clearMoveTime

        animateTrickWinnerAndRemoveTrickFromTable(movePlayed.trickCompleted.trickWinner, showWinnerTime, clearMoveTime)
    }

    let roundCompleteTime = 0
    if (movePlayed.trickCompleted != null && movePlayed.trickCompleted.roundCompleted) {
        roundCompleteTime = 1000
        setTimeout(function () {
            requestForScoreCard();
            if (movePlayed.trickCompleted.gameOver) {
                showInfo("---- GAME OVER ----")
            }
        }, trickCompleteTime)
    }
    if (movePlayed.trickCompleted != null && movePlayed.trickCompleted.gameOver)
        return

    setTimeout(function () {
        requestGameStatus();
        requestLog();
    }, trickCompleteTime + roundCompleteTime)
}

function cardFromHandToTable(movePlayed) {
    let tableCardImage = playerModelToTableImage(movePlayed.player)
    let playerHandCardImage = cardModelToImage(movePlayed.cardPlayed)

    tableCardImage.src = cardModelToImageURL(movePlayed.cardPlayed)
    if (playerHandCardImage == null) {
        playerHandCardImage = lastPlayerHandCardImage(movePlayed.player, movePlayed.cardsInHand)
    }
    shiftCardsToLeft(playerHandCardImage)
    showInfo("")
}

function shiftCardsToLeft(playerHandCardImage) {
    let i = playerHandCardImage.id[playerHandCardImage.id.length-1]
    let name = playerHandCardImage.id.substring(0, playerHandCardImage.id.length-1)
    for (let j=parseInt(i); j < 7; j++) {
        let nb = j + 1
        document.getElementById(name+j).src = document.getElementById(name+nb).src
    }
    document.getElementById(name+7).src = NoCardImage()
}


function doNextMove(nextPlayer) {
    if (isHumanPlayer(nextPlayer)) {
        waitForPlayerMove()
    } else {
        requestComputeMove();
    }
}

function removeCardsFromTable() {
    document.getElementById("tableWest").src = NoCardImage();
    document.getElementById("tableNorth").src = NoCardImage();
    document.getElementById("tableEast").src = NoCardImage();
    document.getElementById("tableSouth").src = NoCardImage();
}

function animateTrickWinnerAndRemoveTrickFromTable(trickWinner, showWinnerWait, clearMoveWait) {
    showLeader(trickWinner)
    setTimeout(function () {
        animateTrickWinner(trickWinner, clearMoveWait)
    }, showWinnerWait);
}

function animateTrickWinner(trickWinner, wait) {
    let postFix = playerModelToElementPostFix(trickWinner)

    animateCardToWinner("tableNorth", "winnerCardNorthTo" + postFix)
    animateCardToWinner("tableWest", "winnerCardWestTo" + postFix)
    animateCardToWinner("tableEast", "winnerCardEastTo" + postFix)
    animateCardToWinner("tableSouth", "winnerCardSouthTo" + postFix)

    setTimeout(function () {
        resetTableCardAnimation("winnerCardNorthTo" + postFix, "tableNorth")
        resetTableCardAnimation("winnerCardWestTo" + postFix, "tableWest")
        resetTableCardAnimation("winnerCardEastTo" + postFix, "tableEast")
        resetTableCardAnimation("winnerCardSouthTo" + postFix, "tableSouth")
    }, wait);
}

function animateCardToWinner(from, to) {
    document.getElementById(from).src = CardBackImage()
    document.getElementById(from).id = to
}

function resetTableCardAnimation(to, from) {
    document.getElementById(to).id = from
    document.getElementById(from).src = NoCardImage();
}

//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------

function handleLog(logLines) {
    //todo: do something with log lines?
}

