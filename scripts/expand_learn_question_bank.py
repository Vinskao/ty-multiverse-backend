#!/usr/bin/env python3
"""Extend the published Learn TOEIC banks to their agreed topic sizes.

The script is idempotent: it only appends missing positions, so it is safe to
run again when seed content is revised or a fresh environment is prepared.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/learn"


def options(correct, distractors, focus):
    keys = "ABCD"
    words = [correct, *distractors]
    return [
        {"key": key, "text": word,
         "rationale": (f"正確。{focus}" if word == correct else f"不正確。此處不符合{focus}")}
        for key, word in zip(keys, words)
    ]


def question(position, section, difficulty, passage_key, passage_text, prompt,
             correct, distractors, focus):
    return {
        "position": position, "section": section, "difficulty": difficulty,
        "derivedFrom": "original", "passageKey": passage_key,
        "passageText": passage_text, "prompt": prompt,
        "options": options(correct, distractors, focus), "correctOption": "A",
        "explanation": f"本題考查{focus}", "focusPoint": focus,
    }


CLOZES = [
 ("supplier-portal", "Supplier portal update\n\nThe purchasing team will launch its updated supplier portal on Monday. Vendors can use the portal to ___ (29) invoices and delivery notices. To avoid interruptions, each vendor should confirm that its contact details are ___ (30) by Friday. A short online demonstration will be held at 3 P.M., and attendance is ___ (31) for new account administrators. Questions received before the session will be ___ (32) during the demonstration.",
  [("submit", ["submitting", "submitted", "submission"], "動詞原形"),("accurate", ["accuracy", "accurately", "accurateness"], "形容詞修飾 contact details"),("required", ["require", "requiring", "requirement"], "be 動詞後的過去分詞形容詞"),("addressed", ["address", "addressing", "addresses"], "被動語態")]),
 ("staff-survey", "Staff survey\n\nEmployees are invited to complete the annual workplace survey. The results will help managers identify areas ___ (33) may need improvement. Responses are anonymous and will be reviewed ___ (34) by the human resources team. Please submit the survey no later ___ (35) May 12. Employees who complete it will be entered into a drawing for a gift card, ___ (36) participation is voluntary.",
  [("that", ["where", "who", "what"], "關係代名詞"),("confidentially", ["confidential", "confidence", "confide"], "副詞修飾 reviewed"),("than", ["then", "that", "as"], "no later than 固定用法"),("although", ["because", "unless", "therefore"], "讓步連接詞")]),
 ("lease-renewal", "Lease renewal notice\n\nYour office lease will expire at the end of September. If you wish to renew, please return the enclosed form ___ (37) August 1. The proposed rate reflects recent improvements to the building, ___ (38) the lobby and elevator system. Tenants choosing a two-year term will receive one month of free parking. Our leasing representative is available to ___ (39) any concerns you may have. We appreciate your continued ___ (40).",
  [("by", ["until", "during", "since"], "截止日期搭配 by"),("including", ["included", "includes", "include"], "現在分詞補充說明"),("discuss", ["discussion", "discussed", "discussing"], "to 後接動詞原形"),("tenancy", ["tenant", "tenanted", "tenacious"], "名詞作受詞")]),
 ("webinar-invitation", "Webinar invitation\n\nJoin us for a webinar on improving customer retention. The presenter will explain how small changes can ___ (41) client satisfaction. Participants will receive a workbook ___ (42) includes practical planning exercises. Registration is free, but space is limited; therefore, reserve a place as soon as ___ (43). A recording will be sent to everyone who registers, ___ (44) they cannot attend live.",
  [("increase", ["increased", "increasing", "increaseable"], "情態動詞後的動詞原形"),("that", ["whose", "where", "whom"], "關係代名詞作主詞"),("possible", ["possibly", "possibility", "possess"], "as soon as possible 固定用法"),("even if", ["because", "so that", "after"], "讓步條件連接詞")]),
 ("shipment-delay", "Shipment delay\n\nWe regret to inform you that your order has been delayed because of severe weather. The shipment is now expected to arrive ___ (45) Thursday afternoon. Our warehouse team is working ___ (46) to process all affected orders. Customers whose orders are delayed for more than five days will receive a shipping credit. We apologize for any ___ (47) this delay may cause and thank you for your ___ (48).",
  [("on", ["at", "in", "by"], "日期前介系詞 on"),("quickly", ["quick", "quicken", "quickness"], "副詞修飾 working"),("inconvenience", ["inconvenient", "convenient", "convenience"], "any 後接名詞"),("patience", ["patient", "patiently", "patent"], "thank you for 後接名詞")]),
 ("copy-center", "Copy center schedule\n\nThe copy center will close early on Friday for equipment maintenance. Requests submitted before noon will be completed the same day; all other requests will be ready ___ (49) Monday morning. Employees with urgent printing needs should contact the center manager ___ (50) advance. The new high-volume printer is expected to be fully operational after the maintenance ___ (51). We appreciate your cooperation while these improvements are being ___ (52).",
  [("by", ["since", "during", "from"], "完成時間搭配 by"),("in", ["at", "on", "for"], "in advance 固定片語"),("work", ["working", "worker", "worked"], "maintenance work 名詞搭配"),("made", ["make", "making", "maker"], "被動進行式")]),
 ("client-newsletter", "Client newsletter\n\nThis month’s newsletter features three companies that have expanded into new markets. Each company developed a plan ___ (53) its local customers’ needs. The article also offers advice from consultants who have worked ___ (54) in international sales. Subscribers may download a planning checklist from our website. Please share the newsletter with colleagues who would find the information ___ (55). Your feedback helps us make future issues even more ___ (56).",
  [("around", ["between", "over", "inside"], "around 表示針對、圍繞"),("extensively", ["extensive", "extent", "extend"], "副詞修飾 worked"),("useful", ["use", "usefully", "usage"], "find + 受詞 + 形容詞"),("valuable", ["value", "valuation", "valuably"], "make + 受詞 + 形容詞")]),
 ("safety-training", "Safety training reminder\n\nAll laboratory employees must complete the annual safety training by June 30. The online course takes approximately 45 minutes and can be paused if necessary. Employees who have not finished the course will receive a reminder ___ (57). Completion records are automatically sent to department supervisors. Please report technical problems ___ (58) the IT help desk. The training is designed to ensure that everyone can respond ___ (59) in an emergency. Thank you for making workplace safety a ___ (60).",
  [("weekly", ["week", "weeks", "weekend"], "副詞表示頻率"),("to", ["at", "for", "with"], "report something to someone"),("appropriately", ["appropriate", "appropriation", "appropriateness"], "副詞修飾 respond"),("priority", ["prior", "prioritize", "primarily"], "make ... a priority 搭配")]),
 ("procurement-review", "Procurement review\n\nThe procurement committee will review all supplier proposals next week. Bidders should ensure that pricing information is ___ (61) listed in the final document. Proposals received after the deadline cannot be considered, ___ (62) the committee will acknowledge their receipt. The committee may ask selected bidders to provide additional details. Final decisions will be announced ___ (63) all evaluations are complete. Suppliers are encouraged to keep copies of every document ___ (64) during the process.",
  [("clearly", ["clear", "clarity", "clearest"], "副詞修飾 listed"),("but", ["so", "because", "unless"], "轉折連接詞"),("once", ["while", "until", "despite"], "once 引導時間子句"),("submitted", ["submitting", "submit", "submission"], "過去分詞修飾 documents")]),
 ("service-hours", "Service hours change\n\nBeginning next month, the customer service desk will open at 8 A.M. instead of 9 A.M. This change was made in response to a ___ (65) number of early-morning calls. The desk will continue to close at 6 P.M. on weekdays. Customers can also find answers to common questions ___ (66) our online help center. Please note that telephone support is not available on public holidays. We expect the earlier opening time to be especially ___ (67) to clients in other time zones. Thank you for choosing our services; we look forward to ___ (68) you.",
  [("growing", ["grow", "grown", "growth"], "現在分詞作形容詞"),("through", ["between", "against", "without"], "through 表示經由"),("helpful", ["help", "helpfully", "helper"], "be especially + 形容詞"),("assisting", ["assist", "assisted", "assistance"], "look forward to 後接動名詞")]),
 ("conference-room", "Conference room booking\n\nConference rooms may be reserved up to three months in advance. When making a reservation, include the expected number of attendees and any equipment ___ (69). Requests for video-conferencing support should be submitted at least two business days ___ (70) the meeting. If a room is no longer needed, please cancel the reservation promptly so it becomes ___ (71) to other teams. Repeated failures to cancel unused reservations may result in temporary ___ (72) of booking privileges.",
  [("needed", ["need", "needing", "need"], "過去分詞作後位修飾"),("before", ["since", "during", "after"], "提前時間搭配 before"),("available", ["availability", "availably", "avail"], "becomes + 形容詞"),("suspension", ["suspend", "suspended", "suspending"], "名詞作受詞")]),
 ("equipment-policy", "Equipment policy\n\nCompany laptops are provided for business use and should be handled with care. Users are responsible for reporting loss or damage ___ (73). Software may be installed only with approval from the technology department. Employees who travel frequently should back up important files ___ (74). The company is not responsible for personal data stored on its devices. Please read the full policy before ___ (75) the equipment agreement. Your signature confirms that you understand these ___ (76).",
  [("immediately", ["immediate", "immediacy", "immediate"], "副詞修飾 reporting"),("regularly", ["regular", "regulation", "regularity"], "副詞修飾 back up"),("signing", ["sign", "signed", "signature"], "介系詞 before 後接動名詞"),("requirements", ["require", "required", "requiring"], "these 後接複數名詞")]),
 ("language-course", "Language course announcement\n\nThe training department is offering a business writing course this autumn. The course is intended for employees who communicate with clients in English ___ (77). Lessons will focus on writing concise e-mails and reports. Participants will practice editing sample documents and will receive individual feedback. To register, complete the online form ___ (78) September 5. Because enrollment is limited, applicants will be accepted in the order ___ (79) their forms are received. Employees who complete all sessions will receive a certificate of ___ (80).",
  [("regularly", ["regular", "regulate", "regularity"], "副詞修飾 communicate"),("by", ["at", "in", "since"], "截止時間用 by"),("in which", ["which", "where", "what"], "the order in which 關係子句"),("completion", ["complete", "completing", "completed"], "of 後接名詞")]),
]


READINGS = [
 ("vendor-audit", "Vendor audit schedule\n\nNorthline Components will conduct its annual vendor audit from July 8 through July 12. Audit staff will review delivery records, product samples, and quality-control procedures. Vendors selected for a visit will receive a proposed schedule at least two weeks beforehand. The company uses the audit results when renewing supply agreements and planning future orders.", [("What is the purpose of the notice?", "To announce an audit process", ["To advertise a new product", "To request job applications", "To report a delivery delay"], "主旨判斷"),("What will vendors receive in advance?", "A proposed visit schedule", ["A supply agreement", "A product sample", "A quality certificate"], "細節定位"),("What can be inferred about audit results?", "They affect future business decisions", ["They are sent to customers", "They replace delivery records", "They are kept confidential from managers"], "推論")]),
 ("museum-hours", "Museum hours update\n\nThe Harbor City Museum will remain open until 8 P.M. on the first Thursday of every month beginning in September. The extended hours coincide with the downtown art walk, and admission after 5 P.M. will be discounted. Guided tours will still end at 4 P.M.; however, visitors may use the audio guide until closing time. The museum café will close at its usual time of 5:30 P.M.", [("What change will occur in September?", "The museum will have monthly evening hours", ["Guided tours will start later", "The café will offer discounts", "Admission will be free every day"], "細節定位"),("Why are the hours being extended?", "To coincide with an art walk", ["To accommodate school groups", "To renovate the galleries", "To host a conference"], "原因判斷"),("What is true about guided tours?", "They end before the museum closes", ["They are offered only at night", "They require an audio guide", "They include café service"], "細節比較")]),
 ("software-migration", "Software migration notice\n\nOn August 16, the accounting department will move to a new expense-reporting system. Employees should submit all outstanding reports in the current system by August 12. Training videos and a list of frequently asked questions are available on the intranet. During the first week after the change, the help desk will offer additional telephone support from 7 A.M. to 7 P.M.", [("What are employees asked to do by August 12?", "Submit outstanding expense reports", ["Watch all training videos", "Call the help desk", "Install new software"], "日期細節"),("Where can employees find training materials?", "On the intranet", ["At the help desk", "In the accounting office", "On a public website"], "資訊定位"),("What will happen after the change?", "Help desk hours will be extended temporarily", ["Expense reports will be reviewed weekly", "Training will be mandatory every evening", "The old system will be upgraded"], "推論")]),
 ("charity-fair", "Charity fair volunteers\n\nThe Riverside Business Association is seeking volunteers for its annual charity fair on October 3. Volunteers may assist with registration, booth setup, or collecting donated items. A brief orientation will take place at 8 A.M. on the day of the event. Anyone interested should complete the online form by September 20 and indicate a preferred assignment.", [("What is the association seeking?", "Volunteers for an event", ["Donations for a new building", "Applications for association membership", "Vendors for a restaurant"], "主旨判斷"),("When is the orientation?", "On the morning of October 3", ["September 20", "The evening before the fair", "One week after the fair"], "日期細節"),("What should applicants include on the form?", "A preferred assignment", ["A résumé", "A donation amount", "A booth design"], "細節定位")]),
 ("travel-policy", "Travel policy reminder\n\nEmployees traveling on company business must book air tickets through the approved travel portal unless a manager grants an exception. The portal compares fares from several airlines and automatically records the trip for expense reporting. Hotel reservations may be made through another service, provided that the nightly rate stays within the regional limit. Questions about exceptions should be directed to the travel coordinator.", [("What must employees normally use to book flights?", "The approved travel portal", ["A manager’s personal account", "Any hotel service", "The expense-reporting system"], "規則細節"),("What does the portal do automatically?", "Records the trip for expenses", ["Approves hotel exceptions", "Chooses a manager", "Sets regional rates"], "細節定位"),("Who handles questions about exceptions?", "The travel coordinator", ["An airline representative", "The hotel manager", "The accounting director"], "人物定位")]),
 ("research-library", "Research library access\n\nBeginning January 4, the Regional Research Library will require visitors to reserve a desk online before arriving. The change is intended to ensure that researchers have adequate workspace during busy periods. Reservations can be made up to fourteen days ahead and may be cancelled without charge until 6 P.M. on the previous day. Visitors who need archived materials should request them when making a reservation.", [("Why is the library introducing reservations?", "To ensure adequate workspace", ["To charge visitors a fee", "To reduce archived materials", "To shorten opening hours"], "原因判斷"),("How far ahead may a desk be reserved?", "Fourteen days", ["One day", "Six months", "Until 6 P.M."], "數字細節"),("When should archived materials be requested?", "When reserving a desk", ["After arriving", "When cancelling", "At the end of the day"], "流程理解")]),
 ("parking-application", "Parking permit applications\n\nApplications for next year’s employee parking permits are now available. Because the number of spaces is limited, permits will be assigned according to a point system based on commuting distance and work schedule. Employees who already have a permit must apply again if they wish to retain access. Results will be posted on the employee portal on November 18.", [("How are permits assigned?", "By a point system", ["In order of application", "By department size", "Through a lottery"], "細節定位"),("Who must submit an application?", "Current permit holders who want continued access", ["Only new employees", "Only managers", "Visitors to the office"], "條件理解"),("Where will results be posted?", "On the employee portal", ["At the parking entrance", "By e-mail only", "In the cafeteria"], "資訊定位")]),
 ("supplier-award", "Supplier recognition award\n\nAt its quarterly meeting, Altair Manufacturing recognized Brightwell Logistics as its supplier of the quarter. Brightwell was selected for maintaining on-time deliveries despite increased demand during the spring. In addition to a certificate, the company will be invited to present its delivery-tracking methods at next month’s supplier forum. Other suppliers may attend the presentation online.", [("Why was Brightwell Logistics selected?", "It maintained timely deliveries", ["It reduced its prices", "It opened a new warehouse", "It hosted a supplier forum"], "原因判斷"),("What will Brightwell do next month?", "Present at a supplier forum", ["Receive a new contract", "Audit other suppliers", "Launch tracking software"], "未來行動"),("How may other suppliers attend?", "Online", ["At a warehouse", "By telephone only", "At the quarterly meeting"], "細節定位")]),
 ("storm-response", "Storm response plan\n\nDue to a forecast of heavy rain, the operations team has prepared a storm response plan for Monday. Field supervisors should check drainage areas before noon and report blocked access routes immediately. If conditions become unsafe, deliveries may be postponed and customers will receive updated arrival estimates by text message. Employees should monitor the company alert page for further instructions.", [("What are field supervisors asked to check?", "Drainage areas", ["Customer text messages", "Delivery estimates", "Employee schedules"], "細節定位"),("What may happen if conditions are unsafe?", "Deliveries may be postponed", ["The alert page will close", "Supervisors will work remotely", "Customers will collect orders"], "條件推論"),("How will customers receive updated estimates?", "By text message", ["By postal mail", "At a meeting", "Through supervisors"], "資訊定位")]),
]


def update_cloze():
    path = ROOT / "toeic-02-cloze.json"
    data = json.loads(path.read_text())
    existing = {q["position"] for q in data["questions"]}
    for item in data["questions"]:
        item["difficulty"] = str(item["difficulty"])
    pos = 29
    for index, (key, text, blanks) in enumerate(CLOZES):
        difficulty = "2" if index < 3 else "3" if index < 8 else "4"
        for correct, distractors, focus in blanks:
            if pos not in existing:
                data["questions"].append(question(pos, "短文填空", difficulty, key, text,
                    "選出最適合填入空格的選項。", correct, distractors, focus))
            pos += 1
    data["questions"].sort(key=lambda q: q["position"])
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n")


def update_reading():
    path = ROOT / "toeic-03-reading.json"
    data = json.loads(path.read_text())
    existing = {q["position"] for q in data["questions"]}
    for item in data["questions"]:
        item["difficulty"] = str(item["difficulty"])
    pos = 14
    for index, (key, text, items) in enumerate(READINGS):
        for prompt, correct, distractors, focus in items:
            if pos not in existing:
                difficulty = "2" if pos <= 20 else "3" if pos <= 30 else "4"
                data["questions"].append(question(pos, "閱讀理解", difficulty, key, text,
                    prompt, correct, distractors, focus))
            pos += 1
    for item in data["questions"]:
        if item["position"] >= 14:
            item["difficulty"] = "2" if item["position"] <= 20 else "3" if item["position"] <= 30 else "4"
    data["questions"].sort(key=lambda q: q["position"])
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n")


if __name__ == "__main__":
    update_cloze()
    update_reading()
