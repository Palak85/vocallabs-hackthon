"""
Dataset Generation Script for Multi-Domain Customer Support NLP Service.
Generates balanced, rich, diverse labeled datasets for:
- 7 Domains (ecommerce, education, insurance, banking, telecom, travel, healthcare)
- Domain-specific hierarchical intents (15 intents per domain)
- Sentiment (positive, neutral, negative)
- Emotion (happy, neutral, concerned, sad, frustrated, angry)
- Urgency (low, medium, high, critical)
- Language (en, hi, hinglish)

Source Attribution: SYNTHETIC
"""

import os
import json
import csv
from datetime import datetime

DATA_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "data"))
METADATA_DIR = os.path.join(DATA_DIR, "metadata")
RAW_DIR = os.path.join(DATA_DIR, "raw")

os.makedirs(DATA_DIR, exist_ok=True)
os.makedirs(METADATA_DIR, exist_ok=True)
os.makedirs(RAW_DIR, exist_ok=True)

# 1. DOMAIN DATASET (Enriched with Hinglish & Brand-Agnostic Phrasing)
DOMAIN_DATA = [
    # ECOMMERCE
    ("My Amazon package hasn't arrived yet.", "ecommerce"),
    ("My Blinkit delivery is late by two hours.", "ecommerce"),
    ("My package is late, where is the courier?", "ecommerce"),
    ("My Amazon package is late.", "ecommerce"),
    ("My Blinkit delivery is late.", "ecommerce"),
    ("My Flipkart order has not arrived.", "ecommerce"),
    ("I received a damaged phone in my delivery box.", "ecommerce"),
    ("The shirt size is wrong, I want to initiate a return.", "ecommerce"),
    ("Where is my order ORD-99214 tracking link?", "ecommerce"),
    ("I want to cancel my order before it ships.", "ecommerce"),
    ("Refund has not been credited to my source account.", "ecommerce"),
    ("Item missing from parcel, only received invoice.", "ecommerce"),
    ("Can I get a replacement for this defective product?", "ecommerce"),
    ("Delivery partner did not call and marked order delivered.", "ecommerce"),
    ("Mera order abhi tak deliver nahi hua.", "ecommerce"),
    ("Mera order abhi tak nahi aaya hai.", "ecommerce"),
    ("Mera order abhi tak nahi aaya.", "ecommerce"),
    ("Parcel kab tak aayega, status update nahi ho raha.", "ecommerce"),
    ("Flipkart order is delayed, please update status.", "ecommerce"),
    ("My cart item is showing out of stock after payment.", "ecommerce"),
    ("The courier delivery guy was rude and refused OTP.", "ecommerce"),
    ("How do I exchange my sneakers for a larger size?", "ecommerce"),
    ("Product quality is extremely poor compared to photo.", "ecommerce"),
    ("Can you change the delivery address for order ORD-112?", "ecommerce"),
    ("Payment deducted for order but order status is failed.", "ecommerce"),
    ("I bought wireless earbuds but the charging case is not working.", "ecommerce"),
    ("Is cash on delivery available for electronic gadgets?", "ecommerce"),
    ("My order was delivered to the wrong house address.", "ecommerce"),
    ("Coupons and promo code not applying at checkout page.", "ecommerce"),
    ("Delivery boy order deke nahi gaya par delivered mark kar diya.", "ecommerce"),
    ("Amazon se mangwaya tha parcel abhi tak nahi aaya.", "ecommerce"),

    # EDUCATION
    ("I need to pay my college semester tuition fees.", "education"),
    ("I need to pay my college fees.", "education"),
    ("My school payment isn't showing.", "education"),
    ("My school fee payment isn't showing in the portal.", "education"),
    ("I paid my daughter's school fees yesterday but portal says unpaid.", "education"),
    ("I paid my college fees yesterday but the portal still shows unpaid.", "education"),
    ("When is the last date for admission form submission?", "education"),
    ("How do I apply for the merit scholarship?", "education"),
    ("The university exam hall ticket has a spelling mistake in my name.", "education"),
    ("Where can I download the semester fee receipt?", "education"),
    ("Fee payment debited from bank but no receipt generated.", "education"),
    ("My attendance is marked incorrect for the math lecture.", "education"),
    ("Can I get a refund of the admission seat booking fee?", "education"),
    ("College bus transport route information needed.", "education"),
    ("Mera college fee pay ho gaya par portal par unpaid dikh raha hai.", "education"),
    ("College ki fees pay ho gayi par portal unpaid dikha raha hai.", "education"),
    ("Semester exam schedule and timetable release date.", "education"),
    ("I want to change my elective course for 4th semester.", "education"),
    ("Hostel fee breakdown and installment options query.", "education"),
    ("Student portal login is locked due to password error.", "education"),
    ("When will the scholarship amount be disbursed to my account?", "education"),
    ("Marksheet correction application process details.", "education"),
    ("School transport fee is deducted twice this quarter.", "education"),
    ("How to apply for transfer certificate from school?", "education"),
    ("Admission form submit nahi ho raha server error aa raha.", "education"),
    ("Exam admit card download nahi ho raha college portal se.", "education"),

    # INSURANCE
    ("My insurance claim CLM-45672 is still pending for three weeks.", "insurance"),
    ("My insurance claim has been pending for three weeks.", "insurance"),
    ("My insurance claim is delayed.", "insurance"),
    ("My policy premium is due tomorrow, how do I pay?", "insurance"),
    ("My policy premium is due.", "insurance"),
    ("Why was my health insurance cashless claim rejected?", "insurance"),
    ("I need to download my car insurance policy document.", "insurance"),
    ("How to add my spouse as nominee in life policy POL-9912?", "insurance"),
    ("What is the waiting period for pre-existing disease coverage?", "insurance"),
    ("I want to cancel my policy and request a premium refund.", "insurance"),
    ("Claim reimbursement amount settled is lower than bill.", "insurance"),
    ("Renewal notice not received for my health policy.", "insurance"),
    ("Hospitalisation cashless approval delayed by TPA desk.", "insurance"),
    ("Mera health insurance claim abhi tak pass nahi hua.", "insurance"),
    ("Claim approve kab tak hoga kuch update do insurance desk.", "insurance"),
    ("Policy document email par nahi mila abhi tak.", "insurance"),
    ("Term insurance cover amount enhancement eligibility query.", "insurance"),
    ("Accidental damage claim inspection surveyor contact number.", "insurance"),
    ("My motor insurance expired last week, how to renew?", "insurance"),
    ("Maturity benefit payout date for endowment policy.", "insurance"),
    ("No claim bonus transfer from previous vehicle insurer.", "insurance"),
    ("Critical illness rider inclusion in existing policy.", "insurance"),
    ("Claim status check for hospitalization bill refund.", "insurance"),
    ("Change communication address in policy record POL-3312.", "insurance"),
    ("Bima policy ka claim status track karna hai.", "insurance"),

    # BANKING
    ("My UPI payment failed but money was deducted from account.", "banking"),
    ("My UPI payment failed.", "banking"),
    ("My UPI transaction failed.", "banking"),
    ("Money was deducted twice for a single grocery transaction.", "banking"),
    ("My debit card is blocked, how to unblock it immediately?", "banking"),
    ("I need last 6 months bank account statement PDF.", "banking"),
    ("Unauthorized ATM cash withdrawal transaction reported.", "banking"),
    ("Net banking password reset OTP is not coming on phone.", "banking"),
    ("Credit card bill payment reversed but limit not restored.", "banking"),
    ("When will my failed IMPS transaction money be refunded?", "banking"),
    ("Home loan interest certificate download issue.", "banking"),
    ("How to enable international transactions on credit card?", "banking"),
    ("Mera UPI payment fail ho gaya aur paise kat gaye.", "banking"),
    ("Account me money debit hua but receiver ko nahi mila.", "banking"),
    ("Cheque bounce charges wrongly levied on savings account.", "banking"),
    ("Fixed deposit maturity amount auto-renewal cancellation.", "banking"),
    ("KYC document verification is still pending after 5 days.", "banking"),
    ("Need to update my registered mobile number in bank account.", "banking"),
    ("Transaction TXN-89912 is stuck in processing state.", "banking"),
    ("Annual debit card maintenance charge refund query.", "banking"),
    ("Bank account se paise cut gaye par transfer nahi hua.", "banking"),
    ("ATM machine me cash atak gaya aur balance cut gaya.", "banking"),

    # TELECOM
    ("My recharge succeeded but mobile data is not working.", "telecom"),
    ("My mobile data is not working.", "telecom"),
    ("My mobile data isn't working.", "telecom"),
    ("No network signal on my SIM card since morning.", "telecom"),
    ("I recharged for 299 pack but unlimited 5G is inactive.", "telecom"),
    ("Frequent call drops in my area, please fix network tower.", "telecom"),
    ("SMS service not working, unable to send bank verification SMS.", "telecom"),
    ("How to port my mobile number to another telecom provider?", "telecom"),
    ("Broadband fiber wifi internet is down with red light on router.", "telecom"),
    ("Postpaid bill amount is showing extra hidden roaming charges.", "telecom"),
    ("Mera recharge ho gaya lekin data nahi chal raha.", "telecom"),
    ("Mera recharge ho gaya lekin internet data nahi chal raha hai.", "telecom"),
    ("SIM card me full signal nahi aa raha hai call disconnect ho rahi hai.", "telecom"),
    ("Validity recharge pack options for prepaid number.", "telecom"),
    ("eSIM activation QR code expired before scanning.", "telecom"),
    ("Caller tune service activated without my consent, deactivate it.", "telecom"),
    ("Data balance exhausted too fast without heavy usage.", "telecom"),
    ("International roaming pack not activated after deduction.", "telecom"),
    ("Fiber installation technician did not arrive at appointment.", "telecom"),
    ("PUK code blocked for my SIM card, need unlock key.", "telecom"),
    ("Net nahi chal raha SIM me signal gayab hai.", "telecom"),
    ("Mobile network issue aa raha hai call drop ho rahi hai.", "telecom"),

    # TRAVEL
    ("My flight 6E-412 was cancelled, need full refund.", "travel"),
    ("My flight was cancelled.", "travel"),
    ("I want to reschedule my flight departure date to next Monday.", "travel"),
    ("Baggage missing at luggage belt in Mumbai airport.", "travel"),
    ("Hotel booking confirmation voucher not received on email.", "travel"),
    ("Boarding pass web check-in is throwing server error.", "travel"),
    ("Can I change passenger name on booked flight ticket?", "travel"),
    ("Train ticket PNR is in RAC, will it confirm before chart?", "travel"),
    ("Flight delay of 4 hours, is refreshment compensation provided?", "travel"),
    ("Mera flight ticket cancel ho gaya refund kab aayega?", "travel"),
    ("Flight cancel ho gayi hai refund kaise aayega?", "travel"),
    ("Hotel check-in denied due to booking discrepancy.", "travel"),
    ("Infant baggage allowance and seat booking inquiry.", "travel"),
    ("Cab driver did not arrive at airport pickup spot.", "travel"),
    ("Seat selection charges deducted twice during web check-in.", "travel"),
    ("Cancellation charges for refundable domestic flight ticket.", "travel"),
    ("Terminal change notice for connecting international flight.", "travel"),
    ("Visa assistance and travel insurance policy for trip.", "travel"),
    ("Lost handbag reported inside flight cabin.", "travel"),
    ("Flight ticket booking cancel karke full refund chahiye.", "travel"),

    # HEALTHCARE
    ("I need to book a doctor appointment with cardiologist.", "healthcare"),
    ("I need to book an appointment with a doctor.", "healthcare"),
    ("I need to book a doctor appointment.", "healthcare"),
    ("When will my blood test lab report be available online?", "healthcare"),
    ("Doctor cancelled my appointment, how do I reschedule?", "healthcare"),
    ("Query regarding dosage instructions on my digital prescription.", "healthcare"),
    ("Hospital admission room billing discrepancy and itemized bill.", "healthcare"),
    ("Is cashless mediclaim accepted for cataract surgery at clinic?", "healthcare"),
    ("Online video consultation doctor did not join call.", "healthcare"),
    ("Prescription medicine delivery is delayed by pharmacy.", "healthcare"),
    ("Doctor appointment reschedule karna hai kal ke liye.", "healthcare"),
    ("Lab test report abhi tak download nahi ho raha portal se.", "healthcare"),
    ("Vaccination slot availability for infant immunization.", "healthcare"),
    ("Need second opinion consultation with neurologist.", "healthcare"),
    ("MRI scan appointment booking and preparation guidelines.", "healthcare"),
    ("Diagnostic center appointment confirmation SMS not received.", "healthcare"),
    ("Prescription refill request for chronic blood pressure tablet.", "healthcare"),
    ("Hospital emergency ambulance contact number request.", "healthcare"),
    ("Doctor ka appointment lena hai specialist clinic me.", "healthcare")
]

# 2. DOMAIN-SPECIFIC INTENTS
def generate_domain_intents():
    intents = {}
    
    # ECOMMERCE
    intents["ecommerce"] = [
        ("Where is my order ORD-99214 right now?", "order_tracking"),
        ("Track my parcel delivery status.", "order_tracking"),
        ("Check tracking location for my shipment.", "order_tracking"),
        ("Mera order tracking details batao.", "order_tracking"),

        ("My Amazon package is late.", "delivery_delay"),
        ("My Blinkit delivery is late.", "delivery_delay"),
        ("My package is late, where is the courier?", "delivery_delay"),
        ("My package delivery is delayed by 3 days.", "delivery_delay"),
        ("Delivery partner is late, please expedite.", "delivery_delay"),
        ("Why is my courier taking so long to arrive?", "delivery_delay"),
        ("Delivery date keeps getting postponed.", "delivery_delay"),
        ("Mera package late ho gaya hai delivery delay.", "delivery_delay"),

        ("I want to cancel my order immediately.", "order_cancellation"),
        ("Please cancel order ORD-7788 and initiate refund.", "order_cancellation"),
        ("Cancel my pending shipment before dispatch.", "order_cancellation"),
        ("Order cancel karna chahta hu turant.", "order_cancellation"),

        ("Where is my refund for returned shoes?", "refund_request"),
        ("Refund of 1499 not received in my bank account.", "refund_request"),
        ("When will my returned item refund be credited?", "refund_request"),
        ("Mera refund status check karke bataiye.", "refund_request"),

        ("Payment failed during checkout but money was deducted.", "payment_failure"),
        ("Amount debited but cart shows payment error.", "payment_failure"),
        ("Checkout payment failed, balance deducted from card.", "payment_failure"),
        ("Payment ho gaya par order place nahi hua.", "payment_failure"),

        ("Received a damaged and cracked laptop screen.", "damaged_product"),
        ("The box was torn and contents were broken inside.", "damaged_product"),
        ("Delivered item is physically broken in shipping.", "damaged_product"),
        ("Parcel me toota hua product mila hai.", "damaged_product"),

        ("Received wrong item, ordered blue shirt got red.", "wrong_product"),
        ("Delivered product is completely different from listing.", "wrong_product"),
        ("Sent wrong size and model for my ordered shoes.", "wrong_product"),
        ("Galat item deliver ho gaya parcel me.", "wrong_product"),

        ("One item missing from multi-item order box.", "missing_product"),
        ("Only 2 out of 3 ordered books were inside package.", "missing_product"),
        ("Missing charger adapter from the electronics box.", "missing_product"),
        ("Package me ek product missing hai.", "missing_product"),

        ("I want to return this dress because size is small.", "return_request"),
        ("How to book a return pickup for defective headphones?", "return_request"),
        ("Need to return this item and get money back.", "return_request"),
        ("Return pickup request schedule karna hai.", "return_request"),

        ("Please replace this defective toaster with new one.", "replacement_request"),
        ("Can I get replacement for broken coffee mug?", "replacement_request"),
        ("Exchange request for a larger shoe size.", "replacement_request"),
        ("Defective unit ko replace karwana hai.", "replacement_request"),

        ("Unable to log in to my shopping account.", "account_problem"),
        ("Password reset link is not arriving on my email.", "account_problem"),
        ("Account is locked due to multiple login attempts.", "account_problem"),
        ("Login nahi ho raha account me error aa raha hai.", "account_problem"),

        ("Very bad experience with your delivery guy and service.", "complaint"),
        ("Worst shopping app, filing complaint for cheating.", "complaint"),
        ("Service is terrible and rude delivery partner behavior.", "complaint"),
        ("Complaint register karni hai ghatiya service ke liye.", "complaint"),

        ("Please transfer this chat to human customer agent.", "human_support_request"),
        ("I want to speak with a human support representative.", "human_support_request"),
        ("Connect me to a live customer service executive.", "human_support_request"),
        ("Mujhe human agent se baat karni hai.", "human_support_request"),

        ("What are the upcoming festive sale discounts?", "general_query"),
        ("Do you provide gift wrap packaging on orders?", "general_query"),
        ("How does membership delivery speed work?", "general_query"),
        ("General question regarding available payment modes.", "general_query"),

        ("Something else about your shopping platform rewards.", "other"),
        ("Miscellaneous inquiry regarding loyalty coins balance.", "other"),
        ("Random feedback about app theme design.", "other"),
        ("Kuch alag query hai regarding store policies.", "other"),
    ]

    # EDUCATION
    intents["education"] = [
        ("How to pay semester tuition fees online?", "fee_payment"),
        ("Need payment link for hostel and mess fees.", "fee_payment"),
        ("I need to pay my college fees.", "fee_payment"),
        ("Where can I pay my term tuition installments?", "fee_payment"),
        ("Online fee payment link open nahi ho raha.", "fee_payment"),

        ("I paid school fees but portal still shows unpaid.", "fee_payment_not_updated"),
        ("My school payment isn't showing.", "fee_payment_not_updated"),
        ("I paid my college fees yesterday but the portal still shows unpaid.", "fee_payment_not_updated"),
        ("I paid my daughter's school fees yesterday but portal says unpaid.", "fee_payment_not_updated"),
        ("Fee payment successful but fee status not updated.", "fee_payment_not_updated"),
        ("Bank debited fee amount but college portal is unpaid.", "fee_payment_not_updated"),
        ("Fees pay ho gayi par portal par update nahi hui.", "fee_payment_not_updated"),

        ("Where can I download the tuition fee receipt?", "fee_receipt"),
        ("Need official stamp on my fee payment receipt PDF.", "fee_receipt"),
        ("Download link for semester fee tax receipt.", "fee_receipt"),
        ("Fee receipt download kaise karein portal se?", "fee_receipt"),

        ("How to apply for refund of security deposit fee?", "fee_refund"),
        ("Withdrew admission, when will fee refund be credited?", "fee_refund"),
        ("Excess fee paid refund application form.", "fee_refund"),
        ("College admission cancel kiya fee refund kab aayega?", "fee_refund"),

        ("What is the admission procedure for B.Tech program?", "admission"),
        ("Last date for submission of entrance admission form.", "admission"),
        ("Direct admission eligibility criteria and cutoff marks.", "admission"),
        ("Admission form fill karne ki last date kya hai?", "admission"),

        ("Eligibility criteria for state government scholarship.", "scholarship"),
        ("Disbursement date for national merit scholarship.", "scholarship"),
        ("How to apply for fee waiver and student scholarship?", "scholarship"),
        ("Scholarship amount kab tak account me aayega?", "scholarship"),

        ("When is the final semester exam timetable coming?", "exam"),
        ("Admit card download error for upcoming university exam.", "exam"),
        ("Exam hall ticket center name correction request.", "exam"),
        ("Semester exam date sheet kab release hogi?", "exam"),

        ("Attendance marked absent even though I was in lecture.", "attendance"),
        ("Minimum attendance percentage required to sit in exams.", "attendance"),
        ("Attendance shortage warning email discrepancy.", "attendance"),
        ("Class me present hone ke baad bhi absent laga diya.", "attendance"),

        ("Syllabus and course curriculum details for MBA.", "course_information"),
        ("Can I change elective subjects in second year?", "course_information"),
        ("Credit requirements for graduation in computer science.", "course_information"),
        ("Course structure aur syllabus details chahiye.", "course_information"),

        ("College bus route and monthly transport pass fees.", "transport"),
        ("College transport bus was late and missed morning lab.", "transport"),
        ("Bus route stop change request for next term.", "transport"),
        ("College bus timings aur stop location janni hai.", "transport"),

        ("Complaint regarding rude behavior of college administration.", "complaint"),
        ("Filing complaint against mess food hygiene quality.", "complaint"),
        ("Poor classroom facilities and broken air conditioning.", "complaint"),
        ("College administration ke against complaint karni hai.", "complaint"),

        ("Connect me to university admission counselor or human.", "human_support_request"),
        ("I need to speak with human helpdesk officer.", "human_support_request"),
        ("Transfer this chat to student support representative.", "human_support_request"),
        ("Helpdesk support executive se baat karaiye.", "human_support_request"),

        ("What are library working hours on weekends?", "general_query"),
        ("Is campus sports complex open for day scholars?", "general_query"),
        ("Hostel curfew timings and visitor rules query.", "general_query"),
        ("General question about university holiday calendar.", "general_query"),

        ("Query regarding student identity card reissue.", "other"),
        ("Other miscellaneous college campus inquiries.", "other"),
        ("Lost property inquiry in college auditorium.", "other"),
        ("Kuch miscellaneous query hai campus facilities ke baare me.", "other"),
    ]

    # INSURANCE
    intents["insurance"] = [
        ("What is the current status of my health insurance claim?", "claim_status"),
        ("Track status of cashless claim CLM-9912.", "claim_status"),
        ("Check reimbursement claim status for surgery bill.", "claim_status"),
        ("Claim number CLM-7712 ka status batayein.", "claim_status"),

        ("My insurance claim is delayed.", "claim_delay"),
        ("My insurance claim has been pending for three weeks.", "claim_delay"),
        ("My insurance claim CLM-45672 is still pending for three weeks.", "claim_delay"),
        ("My hospital claim reimbursement is delayed by 15 days.", "claim_delay"),
        ("Why is the surveyor taking so long to approve motor claim?", "claim_delay"),
        ("Claim processing delay exceeds promised turnaround time.", "claim_delay"),
        ("Claim pending hai do hafte se koi update nahi.", "claim_delay"),

        ("My claim was rejected without clear reason, please review.", "claim_rejection"),
        ("Disputing rejection of surgery claim on pre-existing grounds.", "claim_rejection"),
        ("Why was motor accidental claim repudiated by insurer?", "claim_rejection"),
        ("Claim reject kar diya gaya hai appeal kaise karein?", "claim_rejection"),

        ("My policy premium is due.", "premium_payment"),
        ("How do I pay my annual term insurance premium?", "premium_payment"),
        ("Premium payment failed on portal using debit card.", "premium_payment"),
        ("Direct payment link for life insurance premium.", "premium_payment"),
        ("Policy premium online jama kaise karein?", "premium_payment"),

        ("How to renew my expired car insurance policy?", "policy_renewal"),
        ("Grace period for renewal of health insurance cover.", "policy_renewal"),
        ("Renew term plan with NCB bonus discount.", "policy_renewal"),
        ("Car insurance policy renew karni hai online.", "policy_renewal"),

        ("I want to cancel my policy and surrender insurance.", "policy_cancellation"),
        ("Procedure for cancellation of life insurance bond.", "policy_cancellation"),
        ("Surrender value calculation for endowment policy.", "policy_cancellation"),
        ("Insurance policy band karwani hai surrender kaise karein?", "policy_cancellation"),

        ("Please email my health insurance policy schedule copy.", "policy_document"),
        ("Unable to download e-insurance policy PDF from portal.", "policy_document"),
        ("Original policy certificate download link requested.", "policy_document"),
        ("Policy document copy email par bhej dijiye.", "policy_document"),

        ("Does this health policy cover robotic surgeries and day care?", "coverage_information"),
        ("Maternity coverage and newborn cover policy details.", "coverage_information"),
        ("Critical illness inclusion and sum insured limits.", "coverage_information"),
        ("Policy me kya kya cover hota hai details batayein.", "coverage_information"),

        ("How to update nominee name and bank details in policy?", "nominee_information"),
        ("Change percentage share of nominee in policy record.", "nominee_information"),
        ("Add spouse as secondary nominee in term insurance.", "nominee_information"),
        ("Nominee change karne ka process kya hai?", "nominee_information"),

        ("When will my cancelled policy premium refund arrive?", "refund_request"),
        ("Excess premium deduction refund request.", "refund_request"),
        ("Freelook period policy cancellation refund status.", "refund_request"),
        ("Policy cancel hone ke baad refund kab aayega?", "refund_request"),

        ("Complaint against unfair deduction in hospital claim bill.", "complaint"),
        ("Escalating poor customer service from insurance branch.", "complaint"),
        ("Mis-selling of ULIP policy by insurance agent complaint.", "complaint"),
        ("Insurance company ke against fraud complaint karni hai.", "complaint"),

        ("Transfer this call to an insurance claims officer.", "human_support_request"),
        ("I want to speak directly to a human support agent.", "human_support_request"),
        ("Connect me with customer care manager for claim issue.", "human_support_request"),
        ("Claim officer se baat karwao turant.", "human_support_request"),

        ("What are the network cashless hospitals in Bangalore?", "general_query"),
        ("How is No Claim Bonus calculated on policy renewal?", "general_query"),
        ("Tax deduction 80D limits for senior citizen parents.", "general_query"),
        ("General question about insurance renewal grace period.", "general_query"),

        ("Other policy endorsement inquiries.", "other"),
        ("Miscellaneous insurance coverage query.", "other"),
        ("Change residential address in policy document.", "other"),
        ("Kuch aur jaankari chahiye insurance account ke baare me.", "other"),
    ]

    # BANKING
    intents["banking"] = [
        ("My account is locked due to incorrect netbanking passwords.", "account_problem"),
        ("Unable to access mobile banking application.", "account_problem"),
        ("Net banking user ID is deactivated, how to unlock?", "account_problem"),
        ("Mobile banking app login nahi ho raha account locked hai.", "account_problem"),

        ("My UPI payment failed.", "transaction_failed"),
        ("My UPI transaction failed.", "transaction_failed"),
        ("My UPI payment failed but amount was debited from account.", "transaction_failed"),
        ("ATM transaction failed to dispense cash but balance reduced.", "transaction_failed"),
        ("Card swipe failed at merchant machine but money deducted.", "transaction_failed"),
        ("Paise kat gaye par UPI payment fail dikha raha hai.", "transaction_failed"),

        ("NEFT money transfer is pending for last 8 hours.", "transaction_pending"),
        ("Transaction status shows pending at beneficiary bank.", "transaction_pending"),
        ("IMPS fund transfer is processing and money not received.", "transaction_pending"),
        ("Transaction pending hai kab tak clear hoga?", "transaction_pending"),

        ("When will money reversed from failed payment be credited?", "transaction_reversed"),
        ("Merchant reversed transaction but bank balance not updated.", "transaction_reversed"),
        ("Reversal reference number generated but amount not back.", "transaction_reversed"),
        ("Refund reversal amount kab account me aayega?", "transaction_reversed"),

        ("I was charged twice for the same restaurant POS swipe.", "duplicate_transaction"),
        ("Money was deducted twice for a single grocery transaction.", "duplicate_transaction"),
        ("Double deduction observed on savings account statement.", "duplicate_transaction"),
        ("Single online order charged twice on credit card.", "duplicate_transaction"),
        ("Ek hi payment ke liye do baar paise cut ho gaye.", "duplicate_transaction"),

        ("My credit card was blocked for suspicious activity, unblock it.", "card_problem"),
        ("My debit card is blocked, how to unblock it immediately?", "card_problem"),
        ("Debit card chip not working at ATM machines.", "card_problem"),
        ("Request new contactless debit card replacement.", "card_problem"),
        ("Card block ho gaya hai unblock kaise karein?", "card_problem"),

        ("UPI PIN set error and daily UPI transaction limit exceeded.", "upi_problem"),
        ("Google Pay UPI server error on bank side.", "upi_problem"),
        ("UPI auto-pay mandate cancel request failing.", "upi_problem"),
        ("UPI transactions nahi chal rahi server error aa raha hai.", "upi_problem"),

        ("Refund for failed online merchant transaction not received.", "refund_request"),
        ("Requesting refund of wrongful penalty charges.", "refund_request"),
        ("Annual debit card maintenance charge refund query.", "refund_request"),
        ("Wrong charge laga diya bank ne refund chahiye.", "refund_request"),

        ("How to get password-protected bank account statement PDF?", "account_statement"),
        ("I need last 6 months bank account statement PDF.", "account_statement"),
        ("Need annual interest certificate for income tax filing.", "account_statement"),
        ("Download e-statement for last 3 months savings account.", "account_statement"),
        ("Account statement PDF download kaise karein?", "account_statement"),

        ("Filing formal complaint regarding unauthorized debit charge.", "complaint"),
        ("Worst banking branch service, staff refused assistance.", "complaint"),
        ("Complaint against rude branch manager and long queues.", "complaint"),
        ("Bank branch service ke khilaaf complaint darj karni hai.", "complaint"),

        ("Please connect me to bank customer care executive.", "human_support_request"),
        ("Transfer to a human banking representative.", "human_support_request"),
        ("I need to speak to a phone banking officer.", "human_support_request"),
        ("Bank executive se baat karni hai urgent.", "human_support_request"),

        ("What are the current interest rates for fixed deposits?", "general_query"),
        ("What is the minimum average balance requirement for savings?", "general_query"),
        ("Home loan interest rates for existing customers.", "general_query"),
        ("General question about bank working hours on Saturday.", "general_query"),

        ("Query about locker availability in local branch.", "other"),
        ("Other banking service related inquiries.", "other"),
        ("Cheque book requisition tracking status.", "other"),
        ("Kuch miscellaneous bank queries hain.", "other"),
    ]

    # TELECOM
    intents["telecom"] = [
        ("My recharge payment succeeded but plan not activated.", "recharge_problem"),
        ("My recharge succeeded but balance is missing.", "recharge_problem"),
        ("Recharge failed but money deducted from bank.", "recharge_problem"),
        ("Recharge done on wrong mobile number by mistake.", "recharge_problem"),
        ("Recharge ho gaya par pack activate nahi hua.", "recharge_problem"),

        ("I recharged for 365 days pack but validity not updated.", "recharge_not_updated"),
        ("Plan validity date still shows yesterday after recharge.", "recharge_not_updated"),
        ("Prepaid balance and validity not refreshed after top-up.", "recharge_not_updated"),
        ("Recharge validity portal par update nahi hui.", "recharge_not_updated"),

        ("No network signal on my SIM card since morning.", "network_problem"),
        ("No network signal or emergency calls only showing.", "network_problem"),
        ("Signal bar dropping constantly inside my home.", "network_problem"),
        ("Network coverage issue and frequent call drop in area.", "network_problem"),
        ("SIM card me network signal bilkul nahi aa raha.", "network_problem"),

        ("My mobile data is not working.", "data_problem"),
        ("My mobile data isn't working.", "data_problem"),
        ("My recharge succeeded but mobile data is not working.", "data_problem"),
        ("Mobile 4G/5G data is not opening any website.", "data_problem"),
        ("Data speeds are extremely slow under 100 kbps.", "data_problem"),
        ("Unlimited 5G pack active but throttling to 2G speed.", "data_problem"),
        ("Internet data bilkul nahi chal raha speed slow hai.", "data_problem"),

        ("Outgoing and incoming calls are disconnecting automatically.", "call_problem"),
        ("Frequent call drops in my area, please fix network tower.", "call_problem"),
        ("Unable to make voice calls to landline numbers.", "call_problem"),
        ("Call mute ho jati hai beech me call issue.", "call_problem"),
        ("Calling me problem aa rahi hai voice break ho rahi hai.", "call_problem"),

        ("Incoming SMS for OTP is not arriving on my number.", "sms_problem"),
        ("Outgoing SMS failing with error code 38.", "sms_problem"),
        ("Unable to send SMS for UPI registration and netbanking.", "sms_problem"),
        ("Bank OTP ka SMS nahi aa raha phone par.", "sms_problem"),

        ("What are the best international roaming packs for Dubai?", "plan_information"),
        ("Details of family postpaid plan with shared data.", "plan_information"),
        ("Prepaid tariff plan comparison with OTT subscriptions.", "plan_information"),
        ("Best postpaid plans ki details batayein.", "plan_information"),

        ("I want to downgrade my postpaid plan to lower rental.", "plan_change"),
        ("How to switch from postpaid connection to prepaid?", "plan_change"),
        ("Change active billing plan to annual recharge pack.", "plan_change"),
        ("Postpaid plan change karke dusra plan lena hai.", "plan_change"),

        ("Postpaid bill has extra unauthorized VAS service charges.", "billing_problem"),
        ("Wrong late fee added to my monthly telecom bill.", "billing_problem"),
        ("Disputing extra data usage billing on postpaid number.", "billing_problem"),
        ("Bill me galat charges add ho gaye hain dispute karna hai.", "billing_problem"),

        ("Complaint regarding non-resolution of network ticket.", "complaint"),
        ("Service is terrible, network has been down for 3 days.", "complaint"),
        ("Filing complaint against broadband installation delay.", "complaint"),
        ("Telecom service ke khilaaf complaint register karni hai.", "complaint"),

        ("Connect this chat to a human telecom agent.", "human_support_request"),
        ("I want to speak with customer care supervisor.", "human_support_request"),
        ("Transfer me to a live telecom operator executive.", "human_support_request"),
        ("Customer care representative se connect karein.", "human_support_request"),

        ("How to port my mobile number to another operator?", "general_query"),
        ("How to activate free caller tune on my number?", "general_query"),
        ("Procedure to convert physical SIM to eSIM.", "general_query"),
        ("General question about DND activation process.", "general_query"),

        ("Query regarding duplicate SIM replacement procedure.", "other"),
        ("Other mobile network inquiries.", "other"),
        ("Broadband router password reset instructions.", "other"),
        ("Kuch alag telecom query hai connection ke baare me.", "other"),
    ]

    # TRAVEL
    intents["travel"] = [
        ("I want to book a one-way flight from Delhi to Mumbai.", "booking"),
        ("How to confirm hotel room reservation for 3 nights?", "booking"),
        ("Book train ticket in executive chair car class.", "booking"),
        ("Flight ticket booking karni hai kal ke liye.", "booking"),

        ("Please cancel my flight ticket reservation PNR-8812.", "booking_cancellation"),
        ("How to cancel hotel room booking without penalty?", "booking_cancellation"),
        ("Cancel round trip booking and release seat.", "booking_cancellation"),
        ("Flight ticket cancel karna hai refund ke sath.", "booking_cancellation"),

        ("My flight AI-102 is delayed by 4 hours, what to do?", "flight_delay"),
        ("Connecting flight delayed, will I miss next flight?", "flight_delay"),
        ("Flight departure time postponed by 6 hours.", "flight_delay"),
        ("Flight bahut delay ho gayi hai update batayein.", "flight_delay"),

        ("My flight was cancelled.", "flight_cancellation"),
        ("My flight 6E-412 was cancelled, need full refund.", "flight_cancellation"),
        ("Flight cancelled by airline, need emergency rebooking.", "flight_cancellation"),
        ("Airline cancelled my flight without prior notice.", "flight_cancellation"),
        ("Cancelled flight alternative travel arrangements.", "flight_cancellation"),
        ("Airline ne flight cancel kar di alternative flight do.", "flight_cancellation"),

        ("Where is my flight cancellation refund?", "refund"),
        ("Refund amount credited is less than airline policy.", "refund"),
        ("When will hotel cancellation refund reach my account?", "refund"),
        ("Ticket cancellation refund kab tak account me aayega?", "refund"),

        ("My checked baggage was damaged and torn on conveyor belt.", "baggage_problem"),
        ("Luggage bag did not arrive at destination airport.", "baggage_problem"),
        ("Baggage tag missing and lost suitcase claim.", "baggage_problem"),
        ("Luggage airport par kho gaya hai bag nahi mila.", "baggage_problem"),

        ("Passenger name spelling error on international e-ticket.", "ticket_problem"),
        ("Web check-in boarding pass PDF not generating.", "ticket_problem"),
        ("Seat number not allocated after web check-in.", "ticket_problem"),
        ("Boarding pass download nahi ho raha error aa raha.", "ticket_problem"),

        ("Hotel room AC was broken and bathroom was dirty.", "hotel_problem"),
        ("Hotel refused check-in saying booking is not found.", "hotel_problem"),
        ("Hotel overbooked and denied confirmed room.", "hotel_problem"),
        ("Hotel me check-in nahi de rahe booking mil nahi rahi.", "hotel_problem"),

        ("Complaint against rude cabin crew and terrible ground staff.", "complaint"),
        ("Filing consumer complaint against travel portal for fraud.", "complaint"),
        ("Terrible airport transfer experience and missed tour.", "complaint"),
        ("Airlines staff ke rude behavior ke khilaaf complaint.", "complaint"),

        ("Please connect me to travel agent or human support.", "human_support_request"),
        ("I need to speak directly to airline desk representative.", "human_support_request"),
        ("Transfer this chat to human booking agent.", "human_support_request"),
        ("Travel desk executive se baat karwayein.", "human_support_request"),

        ("What is the free cabin baggage weight limit per passenger?", "general_query"),
        ("Are infant meals provided on international flights?", "general_query"),
        ("Visa requirements for transit passengers in Singapore.", "general_query"),
        ("General question regarding airport terminal departure gates.", "general_query"),

        ("Wheelchair assistance and pet travel policies query.", "other"),
        ("Other travel and booking inquiries.", "other"),
        ("Special meal preference selection on flight.", "other"),
        ("Kuch aur jaankari chahiye travel packages ke baare me.", "other"),
    ]

    # HEALTHCARE
    intents["healthcare"] = [
        ("I need to book an appointment with a doctor.", "appointment"),
        ("I need to book a doctor appointment.", "appointment"),
        ("I want to book an appointment with a dermatologist.", "appointment"),
        ("Schedule a consultation with orthopedic doctor tomorrow.", "appointment"),
        ("Book OPD consultation slot with pediatrician.", "appointment"),
        ("Doctor ka appointment book karna hai kal ke liye.", "appointment"),

        ("Please cancel my doctor consultation appointment for tonight.", "appointment_cancellation"),
        ("Cancel dental checkup appointment and refund booking fee.", "appointment_cancellation"),
        ("Unable to attend doctor appointment, please cancel slot.", "appointment_cancellation"),
        ("Doctor appointment cancel karke refund process karein.", "appointment_cancellation"),

        ("How many times a day should I take this antibiotic tablet?", "prescription_query"),
        ("Doctor forgot to mention syrup dosage on prescription.", "prescription_query"),
        ("Can I substitute prescribed medicine with generic brand?", "prescription_query"),
        ("Dawai kitni baar leni hai prescription me clear nahi hai.", "prescription_query"),

        ("Hospital admission bill includes unavailed nursing charges.", "billing_problem"),
        ("Discrepancy in ICU bed charges and medication billing.", "billing_problem"),
        ("Overcharged on pharmacy medicine bill at discharge.", "billing_problem"),
        ("Hospital bill me extra charge lagaya gaya hai dispute karna hai.", "billing_problem"),

        ("When will my lipid profile blood test report be ready?", "report_query"),
        ("Unable to download MRI scan radiologist report from app.", "report_query"),
        ("Blood test report status pending for 24 hours.", "report_query"),
        ("Lab test report kab tak download ho payegi?", "report_query"),

        ("Is cashless hospitalization available with Star Health insurance?", "insurance_query"),
        ("TPA desk pre-authorization query for scheduled surgery.", "insurance_query"),
        ("Mediclaim approval status at hospital insurance desk.", "insurance_query"),
        ("Hospital me cashless mediclaim facility hai ya nahi?", "insurance_query"),

        ("What is the qualification and OPD timing of Dr. Sharma?", "doctor_information"),
        ("Which specialist doctor is available in evening OPD?", "doctor_information"),
        ("Find senior cardiologist doctor profile and fees.", "doctor_information"),
        ("Doctor ki availability aur OPD timing kya hai?", "doctor_information"),

        ("Complaint regarding 2 hour waiting time despite appointment.", "complaint"),
        ("Doctor was extremely dismissive and rude during consultation.", "complaint"),
        ("Filthy hospital hygiene condition and uncleaned beds.", "complaint"),
        ("Hospital staff ke bad behavior par complaint karni hai.", "complaint"),

        ("Transfer this chat to hospital emergency desk or human agent.", "human_support_request"),
        ("I want to speak with clinic support human representative.", "human_support_request"),
        ("Connect me to hospital helpdesk staff immediately.", "human_support_request"),
        ("Hospital support executive se connect karein.", "human_support_request"),

        ("What are visiting hours for ICU admitted patients?", "general_query"),
        ("Is fasting required before full body blood checkup?", "general_query"),
        ("Vaccination schedule and age limits for toddlers.", "general_query"),
        ("General question about diagnostic lab operating hours.", "general_query"),

        ("Home nurse care and medical equipment rental inquiry.", "other"),
        ("Other clinical or hospital related inquiries.", "other"),
        ("Ambulance service charges and stretcher availability.", "other"),
        ("Kuch alag healthcare assistance query hai.", "other"),
    ]

    return intents

INTENTS_DATA = generate_domain_intents()

# 3. SENTIMENT DATASET
SENTIMENT_DATA = [
    ("Thank you so much, your customer service was excellent!", "positive"),
    ("Great job! My issue was resolved within 10 minutes.", "positive"),
    ("I am very happy with the quick delivery and good packing.", "positive"),
    ("Amazing support team, very polite and helpful.", "positive"),
    ("Everything worked smoothly, thank you for the prompt reply.", "positive"),
    ("I really appreciate the quick refund, 5 stars!", "positive"),
    ("Doctor was very kind and explained everything clearly.", "positive"),
    ("Wonderful experience with your airlines customer desk.", "positive"),
    ("Bahut badhiya service hai, instantly problem solve ho gayi.", "positive"),
    ("Thanks a lot, keep up the great service!", "positive"),
    ("Superb assistance by the phone executive, fully satisfied.", "positive"),
    ("The replacement arrived safely and works perfectly.", "positive"),
    ("Quick response and polite communication, well done.", "positive"),
    ("Fast service and hassle free process, highly recommended.", "positive"),
    
    ("What is the status of my order ORD-1234?", "neutral"),
    ("I want to know the last date for fee payment.", "neutral"),
    ("Please tell me the balance in my savings account.", "neutral"),
    ("How do I update my registered mobile number?", "neutral"),
    ("What are the timings for hospital OPD tomorrow?", "neutral"),
    ("Check the policy renewal amount for motor insurance.", "neutral"),
    ("Can I change my flight departure time?", "neutral"),
    ("Mera claim number CLM-45612 hai, update batayein.", "neutral"),
    ("Provide the IFSC code for Bangalore central branch.", "neutral"),
    ("Where can I download the receipt from portal?", "neutral"),
    ("Is there any discount available on annual plans?", "neutral"),
    ("Please provide the terms and conditions document link.", "neutral"),
    ("Inquiring about normal delivery timeline for my pincode.", "neutral"),
    ("What documents are needed for KYC verification?", "neutral"),
    ("I need to book an appointment with a doctor.", "neutral"),
    ("My flight was cancelled.", "neutral"),
    ("I paid my college fees yesterday but the portal still shows unpaid.", "neutral"),

    ("I am extremely disappointed with your terrible service.", "negative"),
    ("Worst customer support ever, nobody cares about customers.", "negative"),
    ("My money was deducted and nothing is working, this is cheating.", "negative"),
    ("I have contacted you 5 times and received zero help.", "negative"),
    ("I have contacted support four times and nobody is helping me!", "negative"),
    ("This app is completely useless and filled with bugs.", "negative"),
    ("Third class service, my claim is pending for a month.", "negative"),
    ("Bahut ghatiya service hai, koi call pick nahi kar raha.", "negative"),
    ("You guys are wasting my time, unacceptable delay!", "negative"),
    ("My package was stolen and your team refuses to take action.", "negative"),
    ("Pathetic response, I will never use this service again.", "negative"),
    ("Total waste of time and money, extremely frustrated.", "negative"),
    ("Your staff is rude, incompetent and unhelpful.", "negative"),
    ("False promises and zero resolution after repeated followups.", "negative"),
    ("Cheated by hidden charges, highly unethical company.", "negative"),
    ("THIS IS RIDICULOUS! Nobody is helping me!", "negative"),
    ("My Amazon package is late.", "negative"),
    ("My insurance claim has been pending for three weeks.", "negative")
]

# 4. EMOTION DATASET
EMOTION_DATA = [
    # Happy
    ("Thank you! My refund was processed instantly, amazing service.", "happy"),
    ("Very pleased with the fast doctor appointment and friendly staff.", "happy"),
    ("I am glad you resolved my issue so quickly today.", "happy"),
    ("Super happy with my order delivery, arrived before time!", "happy"),
    ("Bahut khush hu, issue turant solve ho gaya.", "happy"),
    ("Wonderful experience, thanks for making this easy!", "happy"),
    ("Delighted with the excellent customer support experience.", "happy"),

    # Neutral
    ("Please inform me about the procedure to reset my password.", "neutral"),
    ("I am checking the status of transaction TXN-1002.", "neutral"),
    ("What is the cost of comprehensive motor insurance?", "neutral"),
    ("Let me know when the exam admit cards are released.", "neutral"),
    ("Just inquiring about normal baggage allowance per passenger.", "neutral"),
    ("Could you provide information on the branch timings?", "neutral"),
    ("Looking for details on the course syllabus structure.", "neutral"),
    ("I need to book an appointment with a doctor.", "neutral"),
    ("I need help.", "neutral"),

    # Concerned
    ("I paid the fees yesterday but it still shows unpaid, worried about deadline.", "concerned"),
    ("I paid my college fees yesterday but the portal still shows unpaid.", "concerned"),
    ("Money was deducted from my account but status is still pending, is it safe?", "concerned"),
    ("My father is unwell and we need lab reports urgently, please help.", "concerned"),
    ("I have not received any confirmation email, hoping my booking went through.", "concerned"),
    ("Thoda chintit hu, payment deduct ho gaya par receipt nahi aayi.", "concerned"),
    ("Worried that my admission might be cancelled if fee is not verified.", "concerned"),
    ("Is my transaction secure? The screen froze during OTP entry.", "concerned"),
    ("My mobile data is not working.", "concerned"),

    # Sad
    ("I lost all my saved money in this failed transfer and feeling helpless.", "sad"),
    ("Missing my sister's wedding because the flight was cancelled without backup.", "sad"),
    ("My scholarship was cancelled unexpectedly and I cannot afford fees now.", "sad"),
    ("Feeling very let down after trusting your platform for so long.", "sad"),
    ("Mera poora plan kharab ho gaya, bahut bura lag raha hai.", "sad"),
    ("So heartbroken that the important medicine did not arrive in time.", "sad"),
    ("Disappointed and helpless as nobody is taking responsibility.", "sad"),

    # Frustrated
    ("I have called your helpline four times already and nobody resolves this!", "frustrated"),
    ("I have contacted support four times and nobody is helping me!", "frustrated"),
    ("Why do I have to explain the same problem to a new person every single time?", "frustrated"),
    ("This is the third ticket I am opening for the exact same delivery issue.", "frustrated"),
    ("Portal keeps throwing errors on the final payment page repeatedly.", "frustrated"),
    ("Baar baar wahi problem aa rahi hai aur koi help nahi kar raha.", "frustrated"),
    ("Endless automated loops without getting any actual resolution!", "frustrated"),
    ("Frustrated with the lack of accountability and repeated delays.", "frustrated"),
    ("My insurance claim has been pending for three weeks.", "frustrated"),

    # Angry
    ("THIS IS RIDICULOUS! Stop giving automated excuses and refund my money NOW!", "angry"),
    ("THIS IS RIDICULOUS! Nobody is helping me!", "angry"),
    ("You people are absolute frauds! I am filing a police complaint today!", "angry"),
    ("I have had enough of your pathetic lies, connect me to your manager immediately!", "angry"),
    ("HOW DARE YOU CANCEL MY TICKET WITHOUT ASKING ME?! TERRIBLE APP!", "angry"),
    ("Yeh kya bakwaas hai! Mera paisa wapas karo warna court jaunga!", "angry"),
    ("SHUT DOWN THIS SCAM SERVICE! I WANT MY FULL REFUND RIGHT NOW!", "angry"),
    ("Disgraceful company! You stole my money and hung up on me!", "angry"),
]

# 5. URGENCY DATASET
URGENCY_DATA = [
    # Low
    ("What are the upcoming discounts next month?", "low"),
    ("Just wondering how reward points can be redeemed.", "low"),
    ("No rush, please reply whenever convenient regarding syllabus.", "low"),
    ("Inquiring about routine policy renewal due next quarter.", "low"),
    ("Kabhi bhi reply kar sakte hain, bas information chahiye thi.", "low"),
    ("Curious about general return policies for future orders.", "low"),
    ("Looking for routine brochure information on courses.", "low"),
    ("I need help.", "low"),

    # Medium
    ("Please check why my delivery is delayed by a day.", "medium"),
    ("My Amazon package is late.", "medium"),
    ("I need to update my email address in account profile.", "medium"),
    ("When can I expect my lab test blood report?", "medium"),
    ("My broadband speed seems slower than usual today.", "medium"),
    ("Kal tak bata dijiye ga mera status kya hai.", "medium"),
    ("Need assistance with fee payment receipt download within 2 days.", "medium"),
    ("Kindly verify the status of my claim submission by tomorrow.", "medium"),
    ("I need to book an appointment with a doctor.", "medium"),

    # High
    ("My flight departs in 3 hours and boarding pass is not downloading!", "high"),
    ("My flight was cancelled.", "high"),
    ("Fee portal closes in 2 hours and payment is stuck, please help!", "high"),
    ("Patient admitted in ICU, need immediate cashless insurance approval!", "high"),
    ("My card was stolen and unauthorized charges are occurring right now!", "high"),
    ("Flight 2 ghante me hai aur ticket confirm nahi hua jaldi help karo!", "high"),
    ("Emergency surgery scheduled today, need cashless authorization instantly!", "high"),
    ("Need access urgently to my bank account for hospital deposit.", "high"),
    ("My mobile data is not working.", "high"),
    ("I have contacted support four times and nobody is helping me!", "high"),

    # Critical
    ("EMERGENCY: Cash deducted 50,000 fraud happening right now, BLOCK ACCOUNT IMMEDIATELY!", "critical"),
    ("CRITICAL MEDICAL EMERGENCY: Ambulance required immediately at location!", "critical"),
    ("STRANDED AT NIGHT ON HIGHWAY: Cab driver abandoned vehicle, need emergency response!", "critical"),
    ("SEVERE SAFETY ISSUE: Hazardous electrical spark from delivered electronic device!", "critical"),
    ("URGENT EMERGENCY: Sab block karo turant, hacking ho rahi hai account me!", "critical"),
    ("LIFE THREATENING SITUATION: Oxygen cylinder delivery failed, immediate action needed!", "critical"),
    ("CRITICAL SECURITY BREACH: Unauthorized access to corporate database reported now!", "critical"),
]

# 6. LANGUAGE DATASET
LANGUAGE_DATA = [
    # English
    ("My Amazon order has not arrived yet.", "en"),
    ("I need to pay my college tuition fees.", "en"),
    ("Please check the status of my insurance claim.", "en"),
    ("My UPI transaction failed during checkout.", "en"),
    ("Where can I download my flight boarding pass?", "en"),
    ("I want to speak with a human support agent.", "en"),
    ("The doctor prescribed medication for blood pressure.", "en"),
    ("Broadband internet connection is disconnected.", "en"),
    ("How do I update my registered phone number?", "en"),
    ("Kindly provide the invoice for my order.", "en"),
    ("What are the cancellation charges for this flight ticket?", "en"),
    ("My account balance is not updating after bank transfer.", "en"),
    ("Where is my order?", "en"),
    ("I need help.", "en"),

    # Hindi (Devanagari)
    ("मेरा आर्डर अभी तक डिलीवर नहीं हुआ है।", "hi"),
    ("मेरा ऑर्डर अभी तक नहीं आया।", "hi"),
    ("मुझे कॉलेज की फीस जमा करनी है।", "hi"),
    ("मेरी बीमा पॉलिसी का क्लेम कब पास होगा?", "hi"),
    ("बैंक खाते से पैसे कट गए लेकिन पेमेंट फेल हो गया।", "hi"),
    ("कृपया मुझे कस्टमर केयर से बात करवाएं।", "hi"),
    ("मेरी फ्लाइट कैंसिल हो गई है रिफंड कब मिलेगा?", "hi"),
    ("डॉक्टर का अपॉइंटमेंट बुक करना है।", "hi"),
    ("मोबाइल का नेटवर्क नहीं आ रहा है।", "hi"),
    ("इंटरनेट की स्पीड बहुत धीमी है।", "hi"),
    ("मेरा पार्सल टूटा हुआ मिला है।", "hi"),
    ("क्या आप मेरी समस्या का समाधान कर सकते हैं?", "hi"),
    ("फीस की रसीद पोर्टल से डाउनलोड नहीं हो रही है।", "hi"),

    # Hinglish (Romanized Hindi-English code-mixed)
    ("Mera order abhi tak deliver nahi hua hai.", "hinglish"),
    ("Mera order abhi tak nahi aaya hai.", "hinglish"),
    ("Mera order abhi tak nahi aaya.", "hinglish"),
    ("College ki fees pay ho gayi par portal unpaid dikha raha hai.", "hinglish"),
    ("Claim approve kab tak hoga kuch update do.", "hinglish"),
    ("Mera UPI payment fail ho gaya aur paise kat gaye.", "hinglish"),
    ("Recharge ho gaya but internet data nahi chal raha hai.", "hinglish"),
    ("Flight cancel ho gayi hai refund kaise aayega?", "hinglish"),
    ("Doctor appointment kal ke liye reschedule karna tha.", "hinglish"),
    ("Kisi human agent se baat karao jaldi.", "hinglish"),
    ("SIM me full signal nahi aa raha bar bar call disconnect ho rahi hai.", "hinglish"),
    ("Hospital bill me extra charge add kar diya hai dispute karna hai.", "hinglish"),
    ("Paise kat gaye account se par receiver ko transfer nahi hua.", "hinglish"),
    ("Ticket cancel kar diya par refund account me nahi aaya.", "hinglish"),
]


def export_csv(filepath, data, headers=["text", "label"]):
    with open(filepath, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(headers)
        for row in data:
            writer.writerow(row)
    print(f"Exported {len(data)} rows to {filepath}")


def generate_all_datasets():
    print("Generating comprehensive multi-domain NLP datasets...")

    export_csv(os.path.join(DATA_DIR, "domain.csv"), DOMAIN_DATA)

    for domain, records in INTENTS_DATA.items():
        fname = f"intent_{domain}.csv"
        export_csv(os.path.join(DATA_DIR, fname), records)

    export_csv(os.path.join(DATA_DIR, "sentiment.csv"), SENTIMENT_DATA)
    export_csv(os.path.join(DATA_DIR, "emotion.csv"), EMOTION_DATA)
    export_csv(os.path.join(DATA_DIR, "urgency.csv"), URGENCY_DATA)
    export_csv(os.path.join(DATA_DIR, "language.csv"), LANGUAGE_DATA)

    metadata = {
        "dataset_version": "1.2.0",
        "created_at": datetime.utcnow().isoformat() + "Z",
        "source": "Curated Synthetic Multi-Domain Corpus for Hackathon Benchmarking",
        "is_synthetic": True,
        "domains_covered": list(INTENTS_DATA.keys()),
        "total_domain_samples": len(DOMAIN_DATA),
        "total_sentiment_samples": len(SENTIMENT_DATA),
        "total_emotion_samples": len(EMOTION_DATA),
        "total_urgency_samples": len(URGENCY_DATA),
        "total_language_samples": len(LANGUAGE_DATA),
        "intents_per_domain_count": {d: len(r) for d, r in INTENTS_DATA.items()},
        "license": "MIT / Academic Hackathon",
        "language_support": ["English", "Hindi", "Hinglish", "Indian English"]
    }

    with open(os.path.join(METADATA_DIR, "dataset_info.json"), "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2)
    print(f"Saved dataset metadata to {os.path.join(METADATA_DIR, 'dataset_info.json')}")


if __name__ == "__main__":
    generate_all_datasets()
