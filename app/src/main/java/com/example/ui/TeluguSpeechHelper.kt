package com.example.ui

import java.util.Locale

object TeluguSpeechHelper {
    // Dictionary of pre-mapped fluent Telugu phonetic pronunciations
    private val phraseMap = mapOf(
        "నమస్కారం రైతు సోదరా! నేను మీ కిసాన్ AI సహాయకుడిని. ఈరోజు మీ ప్రాంతంలో మార్కెట్ ధరలు, వాతావరణం లేదా పంట సలహాలు ఏవైనా అడగండి." to
            "Namaskaram raithu sodaraa! Nenu mee Kisan AI sahaayakudini. Eeroju mee praanthamlo market dharalu, vaathaavaranam leda panta salahalu aevainaa adagandi.",

        "నేడు వరంగల్ ఎనుమాముల యార్డులో తేజ మిర్చి క్వింటాల్‌కు సగటు ధర ₹20,500 పలికింది. గరిష్ట ధర ₹22,800. గుంటూరులో ₹21,800 ఉంది. రాబోయే వారం ధర మరింత పెరిగే అవకాశం (86% విశ్వసనీయత) ఉంది." to
            "Naedu Warangal Enumamula yardulo Teja chilli quintal ku sagatu dhara iravai vela aida vandhalu palikindi. Garishta dhara iravai rendu vela enimidi vandhalu. Guntur lo iravai okati vela enimidi vandhalu undi. Raaboye vaaram dhara marinta perige avakaasham undi.",

        "పత్తి ధర ప్రస్తుతం క్వింటాల్‌కు ₹7,380 గా ఉంది. గులాబీ రంగు పురుగు నివారణకు ఎకరాకు 4-5 లింగాకర్షక బుట్టలు (ఫెరమోన్ ట్రాప్స్) ఏర్పాటు చేసి, ప్రొఫెనోఫాస్ 2 మి.లీ లీటరు నీటికి కలిపి పిచికారీ చేయండి." to
            "Patthi dhara prastutam quintal ku edu vela moodu vandhala enabhai ga undi. Gulaabi rangu purugu nivaaranaku ekaraaku naalugu aidu pheromone traps aerpaatu chesi, Profenofos rendu milli leetarlu leetaru neetiki kalipi pichikaaree cheyandi.",

        "రాబోయే 24 నుండి 36 గంటల్లో తేలికపాటి వర్షం పడే అవకాశం 40% ఉంది. గాలి వేగం గంటకు 14 కి.మీ. ప్రస్తుతం క్రిమిసంహారక మందులు కొట్టవద్దు, కళ్లాల్లో ధాన్యం జాగ్రత్త చేసుకోండి." to
            "Raaboye iravai naalugu nundi muppai aaru gantallo telikapaati varsham pade avakaasham nalabhai shatam undi. Gaali vegam gantaku padhanaalugu kilometerlu. Prastutam mandhulu kottavaddu, kallaallo dhaanyam jaagrattha chesukondi.",

        "వరంగల్ సమీపంలోని శ్రీ లక్ష్మీ కోల్డ్ స్టోరేజీలో 2,100 మెట్రిక్ టన్నుల నిల్వ స్థలం అందుబాటులో ఉంది. నెలవారీ అద్దె క్వింటాల్‌కు ₹110 మాత్రమే. సంప్రదించండి: 0870-2567890." to
            "Warangal sameepam loni Sri Lakshmi Cold Storage lo rendu vela vandha metric tonnula nilva sthalam andubaatulo undi. Nelavaaree addae quintal ku noota padi maatranae.",

        "వరి సోనా మసూరి సగటు ధర క్వింటాల్‌కు ₹2,480 నడుస్తోంది. తేమ 14 శాతం కంటే తక్కువగా ఉండేలా ఎండబెట్టి మార్కెట్‌కు తీసుకువెళ్లండి." to
            "Vari Sona Masoori sagatu dhara quintal ku rendu vela naalugu vandhala enabhai nadustondi. Tema padhanaalugu shaatam kantae thakkuvagaa undaelaa endabetti market ku teesukuvellandi.",

        "మీ ప్రశ్నకు ధన్యవాదాలు. నేటి మార్కెట్ సమాచారం ప్రకారం పత్తి, మిర్చి మరియు పసుపు ధరలు నిలకడగా ఉన్నాయి. పంట కోత మరియు విక్రయాలకు ఇది సరైన సమయం." to
            "Mee prashnaku dhanyavaadaalu. Neti market samaacharam prakaaram patthi, mirchi mariyu pasupu dharalu nilakadagaa unnaayi. Panta kotha mariyu vikrayaalu cheyavachhu."
    )

    fun getSpokenText(text: String, isTeluguEngineAvailable: Boolean): String {
        if (isTeluguEngineAvailable) {
            return text
        }

        // Check if an exact or partial mapped Telugu phrase is found
        phraseMap[text]?.let { return it }

        // If not found in dictionary and contains Telugu characters, transliterate or pronounce
        for ((k, v) in phraseMap) {
            if (text.contains(k.take(20))) {
                return v
            }
        }

        return text
    }
}
