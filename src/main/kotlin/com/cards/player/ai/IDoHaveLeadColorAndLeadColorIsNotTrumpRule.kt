package com.cards.player.ai

import com.cards.game.card.Card

/*

KLEUR BIJLOPEN
==============
kleur bijlopen, maar slag is (en blijft) aan tegenstander (bijv hoogste van kleur uitgekomen. kans op troeven door maat is nihil
  ==> gooi laagste van kleur op die huidige kans op roem zo klein mogelijk maakt
  ==> vbd a, h, b in trick, zelf: v,10 :: beide kaarten zijn na afloop van trick de hoogste. v veel roem, 10 geen roem, dus 10
  ==> vbd trick is 7,10  zelf: 9,b ==> dan b, want 9 heeft kans op 50 en b kans op max 20

kleur bijlopen, maar heb de hoogste van kleur en kleur nog niet gespeeld
  ==> kans op een tweede troefloze (door tegenstander) ronde aanwezig en 10 nog in spel, dan overweeg duiken
  ==> tenzij met duiken kans op roem naar tegenstander groot.
  ==> als 3 kaarten in hand en maat is gekomen, dan gooi aas
  ==> vbd trick 7,v   zelf:8,a  dan toch aas niet duiken, zelfs bij allereerste slag, vooral als maat is gekomen.

kleur bijlopen, maar heb de hoogste van kleur en kleur is wel al gespeeld
   ==> hoogste kans op roem doen, hoogste kaart, tenzij kans op troeven
   ==> als kan op nog een keer duiken zou lonen, dan nog een keer duiken
          voorwaarde: geen troeven meer.
          vrij zeker dat 10 bij voorloper is (niet in de achterhand)
              de rule hiervoor is: voorloper is eerder met deze kleur uitgekomen.

kleur bijlopen, laatste speler, heb 10, en aas er nog in
  ==> gooi 10

 eerste ronde van de kleur, en a, 10 nog niet gegooid en zelf geen A,10 en onduidelijk waar a is
 vbd trick is v :: zelf h,9,8 ==> speel op safe: ontwijk roem (dus 8), speel risky: op de roem (dus h)
                              ==> complexer: weet je dat je nat gaat,dan ontwijken, als enige kans op niet nat, dan doen
                              ==> ook: als maat gaan, voorzichtiger zijn.
 vbd trick is b :: 7,8        ==>  maakt niet veel uit. 8 geeft kleine kans op nu roem, maar toekomstige kans op roem is nul door eigen kaart
                                                        7 geeft geen kans op roem (door eigen toedoen), maar wel toekomstige kans op roem
                                                        ik zou nu kiezen voor de 8

 eerste ronde van de kleur, en a, 10 nog niet gegooid heb zelf geen A, maar wel 10 en onduidelijk waar a is
 zo laag mogelijk, ontwijk roem.
 vbd: trick is h, zelf 10,v ==> toch de v

 kleur bijlopen, slag aan maat, naast jouw kaarten alles van die kleur gespeeld:
 ==> gooi hoogste vd kleur, zoveel mogelijk roem

kleur bijlopen, slag aan maat, en blijft aan maat, eerste ronde van die kleur
 ==> gooi hoogste van de kleur, zeker als je daarna de hoogste nog over houdt
 ==> maak kans op roem in huidge trick zo grot mogelijk.
 vbd: trick: a,9    zelf 10,v,h ==> gooi 10 (en hoogste kaart en hoogste roem mogelijkheid)



 ALGEMEEN:
 hoogste roem mogelijkheid: 20 zeker is altijd hoger dan 50 misschien.
 overweeg duiken: Veel van de kleur nog in spel (bij tegenstanders).
                  Zelf niet heel veel van de kleur.
                  kans op pit niet meer aanwezig (na 4 rondjes?)

 */

class IkHebWelLeadColorEnDatIsGeenTroefRule(player: GeniusPlayerKlaverjassen, brainDump: BrainDump): AbstractPlayerRules(player, brainDump) {


    //------------------------------------------------------------------------------------------------------------------

    override fun chooseCard(): Card {
        return playFallbackCard()
    }

    //------------------------------------------------------------------------------------------------------------------
}