package com.mty.bangcalendar.enum

enum class EventConstant(val id: Int, val describe: String) {

    //活动属性
    PURE(1, "Pure"),
    HAPPY(2, "Happy"),
    COOL(3, "Cool"),
    POWERFUL(4, "Powerful"),

    //活动类型
    NORMAL(1, "一般活动（协力）"),
    CHALLENGE(2, "挑战LIVE（CP）"),
    FIGHT(3, "竞演LIVE（对邦）"),
    EX(4, "LIVE试炼（EX）"),
    MISSION(5, "任务LIVE（协力）"),
    GROUP(6, "组曲LIVE（3组曲）"),
    MULTI(7, "团队LIVE（5v5）")

}