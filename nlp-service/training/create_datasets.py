"""
Comprehensive Multi-Domain Dataset Generator for Customer Support NLP System.
Generates realistic, balanced, multi-lingual datasets (English, Hindi, Hinglish)
supporting the enriched intent taxonomy, domain detection, sentiment, emotion, and urgency.
"""

import os
import json
import pandas as pd

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
DATA_DIR = os.path.join(BASE_DIR, "data")
METADATA_DIR = os.path.join(DATA_DIR, "metadata")
os.makedirs(METADATA_DIR, exist_ok=True)

# 1. DOMAIN DATASET
DOMAIN_DATA = [
    # ECOMMERCE
    ("Where is my order ORD-99214 right now?", "ecommerce"),
    ("My order ORD12345 has not arrived yet and it was supposed to be delivered yesterday.", "ecommerce"),
    ("I received a damaged phone in my delivery package.", "ecommerce"),
    ("I want to return the shoes I received yesterday.", "ecommerce"),
    ("Can I get a refund for my cancelled order ORD-8812?", "ecommerce"),
    ("Mera order abhi tak deliver nahi hua ORD-7721.", "ecommerce"),
    ("Wrong color jacket delivered, need replacement immediately.", "ecommerce"),
    ("Product missing from my shipment box.", "ecommerce"),
    ("When will my Amazon package reach my address?", "ecommerce"),
    ("Courier delivery boy asked for extra money on prepaid order.", "ecommerce"),
    ("How to initiate return pickup for electronic items?", "ecommerce"),
    ("Order cancel karne ke baad refund kab aayega?", "ecommerce"),
    ("Defective toaster received, replace with new unit.", "ecommerce"),
    ("Invoice bill was not included in my package.", "ecommerce"),
    ("Galat item deliver ho gaya parcel me replacement do.", "ecommerce"),
    ("Order dispatch ho gaya hai par tracking link nahi chal rahi.", "ecommerce"),
    ("Parcel box was torn and items were broken inside.", "ecommerce"),
    ("Exchange request for a larger shirt size.", "ecommerce"),
    ("Refund status showing processed but money not in wallet.", "ecommerce"),
    ("What are the upcoming festive sale discounts?", "ecommerce"),

    # EDUCATION
    ("I paid my college fee yesterday but it is still showing unpaid.", "education"),
    ("I paid school fees but portal still shows unpaid.", "education"),
    ("How to pay semester tuition fees online?", "education"),
    ("Where can I download the tuition fee receipt PDF?", "education"),
    ("Withdrew admission, when will fee refund be credited?", "education"),
    ("What is the admission procedure for B.Tech computer science?", "education"),
    ("Eligibility criteria for state government merit scholarship.", "education"),
    ("When is the final semester exam timetable coming?", "education"),
    ("Need fee payment installment plan for second term.", "education"),
    ("Admit card download error for upcoming university exam.", "education"),
    ("Hostel and mess fee structure details for first year.", "education"),
    ("Kaise bhai school ka kya fee h?", "education"),
    ("I paid my school fees.", "education"),
    ("Maine aaj subah apni college fees ₹85,000 pay ki dono transactions deduct ho gaye total ₹1,70,000 chala gaya duplicate fee payment refund.", "education"),
    ("College fees deducted twice from bank account for same semester.", "education"),
    ("Paid college fees two times by mistake, need refund for second transaction.", "education"),
    ("Duplicate fee payment deduction on college portal, second payment successful first pending.", "education"),
    ("College fee double debit issue, two times deducted from account.", "education"),
    ("Fees do baar cut ho gayi account se ek hi receipt mili hai refund chahiye.", "education"),
    ("Failed fee transaction ka amount abhi tak account me wapas nahi aaya refund timeline batao.", "education"),
    ("College fee receipt generate nahi hui payment complete hone ke baad bhi.", "education"),
    ("Today is the last date for fee payment and portal is showing payment error.", "education"),
    ("Fee deadline issue: portal down on last day of fee submission.", "education"),

    # INSURANCE
    ("When will my insurance expire?", "insurance"),
    ("I want to check status of my health insurance claim CLM-9921.", "insurance"),
    ("My car accident insurance claim was rejected unfairly.", "insurance"),
    ("How to renew my two-wheeler comprehensive insurance policy?", "insurance"),
    ("Need cashless hospital list in Bangalore for Star Health.", "insurance"),
    ("What is the grace period for term life insurance premium payment?", "insurance"),
    ("Download soft copy of my active health insurance policy document.", "insurance"),
    ("Claim reimbursement amount credited is less than approved sum.", "insurance"),
    ("Add my newborn child as a beneficiary in family floater policy.", "insurance"),
    ("Mera health insurance claim pending hai 10 din se.", "insurance"),
    ("Policy renewal payment cut gaya par policy document nahi mila.", "insurance"),
    ("TPA desk pre-authorization form for planned knee surgery.", "insurance"),
    ("No Claim Bonus discount percentage for current renewal year.", "insurance"),
    ("How to file critical illness claim under health insurance?", "insurance"),
    ("Car insurance policy lapse ho gayi hai renew kaise karein?", "insurance"),
    ("Is robotic surgery covered under my existing health policy?", "insurance"),
    ("Mera claim reject ho gaya dispute kaise file karein?", "insurance"),
    ("Premium receipt for 80D income tax deduction download.", "insurance"),
    ("Address change request in life insurance policy.", "insurance"),
    ("Network hospital me cashless approval nahi de rahe TPA wale.", "insurance"),

    # BANKING
    ("My UPI transaction failed but the money was deducted from my account.", "banking"),
    ("Hey I have to pay EMI this month from my bank.", "banking"),
    ("Money was debited from my account but receiver did not receive it.", "banking"),
    ("₹75,000 do baar debit ho gaya. Merchant ko sirf ek payment mili. Second payment failed thi lekin amount deduct ho gaya aur refund nahi aaya.", "banking"),
    ("₹40,000 account se deduct hua but receiver ko nahi mila.", "banking"),
    ("₹75,000 payment was deducted twice for a single order swipe.", "banking"),
    ("Amount deducted twice for same transaction on Google Pay UPI.", "banking"),
    ("I was charged twice for the same restaurant POS swipe.", "banking"),
    ("Debit card blocked after three incorrect ATM PIN attempts.", "banking"),
    ("Credit card unauthorized international transaction alert received.", "banking"),
    ("Download bank account statement PDF for last 6 months.", "banking"),
    ("Net banking password reset OTP is not coming on phone.", "banking"),
    ("Mera UPI payment fail ho gaya aur paise kat gaye.", "banking"),
    ("mera UPI fail ho gaya paisa kat gaya", "banking"),
    ("Account me money debit hua but receiver ko nahi mila.", "banking"),
    ("Cheque bounce charges wrongly levied on savings account.", "banking"),
    ("Fixed deposit maturity amount auto-renewal cancellation.", "banking"),
    ("Transaction TXN-89912 is stuck in processing state.", "banking"),
    ("Bank account se paise cut gaye par transfer nahi hua.", "banking"),
    ("ATM machine me cash atak gaya aur balance cut gaya.", "banking"),

    # TELECOM
    ("I recharged my number but the recharge has not been updated.", "telecom"),
    ("I recharged my mobile number but validity not updated.", "telecom"),
    ("Bhai maine ₹599 ka recharge kiya tha. Payment successful hai aur bank se paise deduct ho gaye, lekin telecom account mein recharge update nahi hua. Purana plan hi show ho raha hai.", "telecom"),
    ("bhai mera recharge nahi hua", "telecom"),
    ("mera recharge abhi tak nahi hua", "telecom"),
    ("Recharge done but pack not activated on SIM.", "telecom"),
    ("My recharge succeeded but mobile data is not working.", "telecom"),
    ("My mobile data is not working.", "telecom"),
    ("No network signal on my SIM card since morning.", "telecom"),
    ("I recharged for 299 pack but unlimited 5G is inactive.", "telecom"),
    ("Frequent call drops in my area, please fix network tower.", "telecom"),
    ("SMS service not working, unable to send bank verification SMS.", "telecom"),
    ("How to port my mobile number to another telecom provider?", "telecom"),
    ("Broadband fiber wifi internet is down with red light on router.", "telecom"),
    ("Postpaid bill amount is showing extra hidden roaming charges.", "telecom"),
    ("Mera recharge ho gaya lekin data nahi chal raha.", "telecom"),
    ("SIM card me full signal nahi aa raha hai call disconnect ho rahi hai.", "telecom"),
    ("eSIM activation QR code expired before scanning.", "telecom"),
    ("Net nahi chal raha SIM me signal gayab hai.", "telecom"),
    ("Mobile network issue aa raha hai call drop ho rahi hai.", "telecom"),

    # TRAVEL
    ("My flight was cancelled and I need a refund.", "travel"),
    ("My flight 6E-412 was cancelled, need full refund.", "travel"),
    ("My flight was cancelled.", "travel"),
    ("meri flight cancel ho gayi refund chahiye", "travel"),
    ("Flight cancel ho gayi hai refund kaise aayega?", "travel"),
    ("I want to reschedule my flight departure date to next Monday.", "travel"),
    ("Baggage missing at luggage belt in Mumbai airport.", "travel"),
    ("Hotel booking confirmation voucher not received on email.", "travel"),
    ("Boarding pass web check-in is throwing server error.", "travel"),
    ("Can I change passenger name on booked flight ticket?", "travel"),
    ("Train ticket PNR is in RAC, will it confirm before chart?", "travel"),
    ("Flight delay of 4 hours, is refreshment compensation provided.", "travel"),
    ("Mera flight ticket cancel ho gaya refund kab aayega?", "travel"),
    ("Hotel check-in denied due to booking discrepancy.", "travel"),
    ("Flight ticket booking cancel karke full refund chahiye.", "travel"),
    ("Lost handbag reported inside flight cabin.", "travel"),
    ("Seat selection charges deducted twice during web check-in.", "travel"),

    # HEALTHCARE
    ("I need to cancel my appointment with Dr Sharma tomorrow.", "healthcare"),
    ("Cancel my appointment with Dr Sharma tomorrow.", "healthcare"),
    ("i have emergency to go out so cancel me appointment with dr harsh", "healthcare"),
    ("I need to book a doctor appointment with cardiologist.", "healthcare"),
    ("Mere family member ki condition suddenly serious ho gayi hai. Hume urgently hospital jaana hai aur emergency appointment pending hai.", "healthcare"),
    ("Bhai mere family member ki condition suddenly serious ho gayi hai aur humein urgently hospital le jaana hai emergency appointment pending.", "healthcare"),
    ("Hospital me emergency doctor appointment book karni hai jaldi.", "healthcare"),
    ("Emergency medical assistance needed for chest pain patient.", "healthcare"),
    ("When will my blood test lab report be available online?", "healthcare"),
    ("Doctor cancelled my appointment, how do I reschedule?", "healthcare"),
    ("Query regarding dosage instructions on my digital prescription.", "healthcare"),
    ("Hospital admission room billing discrepancy and itemized bill.", "healthcare"),
    ("Is cashless mediclaim accepted for cataract surgery at clinic?", "healthcare"),
    ("Doctor appointment reschedule karna hai kal ke liye.", "healthcare"),
    ("Lab test report abhi tak download nahi ho raha portal se.", "healthcare"),
    ("Vaccination slot availability for infant immunization.", "healthcare"),
    ("Diagnostic center appointment confirmation SMS not received.", "healthcare"),
    ("Hospital emergency ambulance contact number request.", "healthcare")
]

# 2. DOMAIN-SPECIFIC INTENTS
def generate_domain_intents():
    intents = {}

    # BANKING INTENTS
    intents["banking"] = [
        # duplicate_transaction
        ("₹75,000 do baar debit ho gaya. Merchant ko sirf ek payment mili. Second payment failed thi lekin amount deduct ho gaya aur refund nahi aaya.", "duplicate_transaction"),
        ("₹75,000 payment was deducted twice for a single order swipe.", "duplicate_transaction"),
        ("Amount deducted twice for same transaction on Google Pay UPI.", "duplicate_transaction"),
        ("I was charged twice for the same restaurant POS swipe.", "duplicate_transaction"),
        ("Money was deducted twice for a single grocery transaction.", "duplicate_transaction"),
        ("Double deduction observed on savings account statement.", "duplicate_transaction"),
        ("Single online order charged twice on credit card.", "duplicate_transaction"),
        ("Ek hi payment ke liye do baar paise cut ho gaye refund chahiye.", "duplicate_transaction"),
        ("Duplicate payment debit ho gaya account se merchant received once.", "duplicate_transaction"),

        # debit_but_receiver_not_received
        ("₹40,000 account se deduct hua but receiver ko nahi mila.", "debit_but_receiver_not_received"),
        ("Money was debited from my account but receiver did not receive it.", "debit_but_receiver_not_received"),
        ("Account se paise kat gaye par beneficiary account me credit nahi hua.", "debit_but_receiver_not_received"),
        ("Paid shopkeeper via UPI, money deducted from me but shopkeeper didn't get it.", "debit_but_receiver_not_received"),
        ("Merchant says payment not received though my bank balance is debited.", "debit_but_receiver_not_received"),
        ("Receiver ko paise nahi mile mere account se cut ho gaye hain.", "debit_but_receiver_not_received"),

        # transaction_failed
        ("My UPI transaction failed but the money was deducted from my account.", "transaction_failed"),
        ("UPI transaction failed ho gaya aur payment complete nahi hui.", "transaction_failed"),
        ("UPI payment failed and amount not credited back to bank.", "transaction_failed"),
        ("IMPS fund transfer failed with error code 91.", "transaction_failed"),
        ("Payment failed at merchant terminal but amount debited.", "transaction_failed"),
        ("Mera UPI payment fail ho gaya aur paise kat gaye.", "transaction_failed"),

        # transaction_pending
        ("Transaction TXN-89912 is stuck in processing pending state.", "transaction_pending"),
        ("UPI payment status is showing pending since 2 hours.", "transaction_pending"),
        ("NEFT transfer pending clearance from RBI switch.", "transaction_pending"),
        ("Payment pending show ho rahi hai confirm nahi hui.", "transaction_pending"),

        # refund_not_received
        ("Refund for failed online merchant transaction not received.", "refund_not_received"),
        ("Refund not credited back to account after 7 working days.", "refund_not_received"),
        ("Reversal amount for failed ATM withdrawal not received.", "refund_not_received"),
        ("Merchant initiated refund but money not showing in bank balance.", "refund_not_received"),
        ("Refund abhi tak bank account me nahi aaya.", "refund_not_received"),

        # unauthorized_transaction
        ("Credit card unauthorized international transaction alert received.", "unauthorized_transaction"),
        ("Fraudulent debit of 20000 on my debit card without OTP.", "unauthorized_transaction"),
        ("My account was hacked and unauthorized withdrawal occurred.", "unauthorized_transaction"),
        ("Anjaan transaction hua hai mere account se fraud report karna hai.", "unauthorized_transaction"),

        # account_debit_issue
        ("Wrong penalty charges debited from my savings account.", "account_debit_issue"),
        ("Minimum balance penalty deducted unfairly.", "account_debit_issue"),
        ("Unknown auto-debit charge on bank statement.", "account_debit_issue"),

        # card_payment_issue
        ("Debit card blocked after three incorrect ATM PIN attempts.", "card_payment_issue"),
        ("My credit card was blocked for suspicious activity, unblock it.", "card_payment_issue"),
        ("Card swipe declined at retail store POS machine.", "card_payment_issue"),
        ("ATM card not working chip read error.", "card_payment_issue"),

        # cash_withdrawal_issue
        ("ATM machine me cash atak gaya aur balance cut gaya.", "cash_withdrawal_issue"),
        ("Cash not dispensed from ATM but account was debited.", "cash_withdrawal_issue"),
        ("ATM machine deducted money but did not give cash.", "cash_withdrawal_issue"),

        # account_statement
        ("Download bank account statement PDF for last 6 months.", "account_statement"),
        ("How to get password-protected bank account statement PDF?", "account_statement"),
        ("Need annual interest certificate for income tax filing.", "account_statement"),
        ("Account statement PDF download kaise karein?", "account_statement"),

        # loan_emi
        ("Hey I have to pay EMI this month from my bank.", "loan_emi"),
        ("When is my home loan EMI due date this month?", "loan_emi"),
        ("How to set up auto-debit for personal loan EMI?", "loan_emi"),
        ("Loan EMI payment options and interest rate query.", "loan_emi"),

        # other
        ("What are the current interest rates for fixed deposits?", "other"),
        ("Query about locker availability in local branch.", "other"),
        ("Cheque book requisition tracking status.", "other"),
    ]

    # EDUCATION INTENTS
    intents["education"] = [
        # duplicate_fee_payment
        ("Maine aaj subah apni college fees ₹85,000 pay ki dono transactions deduct ho gaye total ₹1,70,000 chala gaya duplicate fee payment refund.", "duplicate_fee_payment"),
        ("₹85,000 fee payment do baar debit hui, college ko ek payment mili aur first payment ka refund nahi aaya.", "duplicate_fee_payment"),
        ("College fees deducted twice from bank account for same semester.", "duplicate_fee_payment"),
        ("Paid college fees two times by mistake, need refund for second transaction.", "duplicate_fee_payment"),
        ("Duplicate fee payment deduction on college portal, second payment successful first pending.", "duplicate_fee_payment"),
        ("College fee double debit issue, two times deducted from account.", "duplicate_fee_payment"),
        ("Fees do baar cut ho gayi account se ek hi receipt mili hai refund chahiye.", "duplicate_fee_payment"),
        ("Duplicate fee payment reversal request on student accounting portal.", "duplicate_fee_payment"),

        # fee_payment_not_updated
        ("I paid my college fee yesterday but it is still showing unpaid.", "fee_payment_not_updated"),
        ("I paid school fees but portal still shows unpaid.", "fee_payment_not_updated"),
        ("Payment successful hai but college portal par fee update nahi hui.", "fee_payment_not_updated"),
        ("Bank debited fee amount but college portal is unpaid.", "fee_payment_not_updated"),
        ("Fees pay ho gayi par portal par update nahi hui.", "fee_payment_not_updated"),

        # fee_payment_refund_pending
        ("Failed fee payment ka refund abhi tak nahi aaya.", "fee_payment_refund_pending"),
        ("College fee refund for failed payment not credited to bank.", "fee_payment_refund_pending"),
        ("Excess fee paid refund application status pending.", "fee_payment_refund_pending"),
        ("Admission withdrawal fee refund kab tak aayega?", "fee_payment_refund_pending"),

        # fee_receipt_not_generated
        ("Fee payment successful but fee receipt not generated.", "fee_receipt_not_generated"),
        ("Unable to download fee receipt PDF after successful payment.", "fee_receipt_not_generated"),
        ("Payment ho gayi par receipt generate nahi hui portal par.", "fee_receipt_not_generated"),

        # fee_deadline_issue
        ("Today is last date to submit college fees and payment gateway failing.", "fee_deadline_issue"),
        ("Fee deadline today need urgent extension to pay.", "fee_deadline_issue"),
        ("College fee payment submission last date issue.", "fee_deadline_issue"),

        # fee_payment
        ("How to pay semester tuition fees online?", "fee_payment"),
        ("Need payment link for hostel and mess fees.", "fee_payment"),
        ("I need to pay my college fees.", "fee_payment"),
        ("Where can I pay my term tuition installments?", "fee_payment"),

        # admission
        ("What is the admission procedure for B.Tech computer science?", "admission"),
        ("Last date for submission of entrance admission form.", "admission"),
        ("Admission form fill karne ki last date kya hai?", "admission"),

        # scholarship
        ("Eligibility criteria for state government scholarship.", "scholarship"),
        ("Disbursement date for national merit scholarship.", "scholarship"),
        ("Scholarship application status and verification.", "scholarship"),

        # exam / result / certificate
        ("When is the final semester exam timetable coming?", "exam"),
        ("Admit card download error for upcoming university exam.", "exam"),
        ("Semester examination result declaration date.", "result"),
        ("Degree certificate and provisional mark sheet request.", "certificate"),

        # other
        ("Kaise bhai school ka kya fee h?", "other"),
        ("School and college curriculum course structure inquiry.", "other"),
    ]

    # HEALTHCARE INTENTS
    intents["healthcare"] = [
        # emergency_appointment
        ("Mere family member ki condition suddenly serious ho gayi hai. Hume urgently hospital jaana hai aur emergency appointment pending hai.", "emergency_appointment"),
        ("Bhai mere family member ki condition suddenly serious ho gayi hai aur humein urgently hospital le jaana hai emergency appointment pending.", "emergency_appointment"),
        ("Family member ki condition serious hai aur emergency appointment pending hai.", "emergency_appointment"),
        ("Hospital me emergency appointment slot book karni hai jaldi.", "emergency_appointment"),
        ("Urgent emergency doctor appointment required for serious patient.", "emergency_appointment"),
        ("Emergency appointment slot booking pending payment deducted.", "emergency_appointment"),

        # emergency_medical_assistance
        ("Emergency medical assistance needed for heart attack symptoms immediately.", "emergency_medical_assistance"),
        ("Patient unconscious call ambulance and emergency medical team now.", "emergency_medical_assistance"),
        ("Severe bleeding and critical condition need immediate emergency medical help.", "emergency_medical_assistance"),
        ("Hospital emergency ambulance contact number request urgently.", "emergency_medical_assistance"),

        # appointment_cancellation
        ("I need to cancel my appointment with Dr Sharma tomorrow.", "appointment_cancellation"),
        ("Cancel my appointment with Dr Sharma tomorrow.", "appointment_cancellation"),
        ("i have emergency to go out so cancel me appointment with dr harsh", "appointment_cancellation"),
        ("Please cancel my doctor consultation appointment for tonight.", "appointment_cancellation"),
        ("Doctor appointment cancel karke refund process karein.", "appointment_cancellation"),

        # appointment_booking
        ("I need to book a doctor appointment with cardiologist.", "appointment_booking"),
        ("I need to book an appointment with a doctor.", "appointment_booking"),
        ("Schedule a consultation with orthopedic doctor tomorrow.", "appointment_booking"),
        ("Book OPD consultation slot with pediatrician.", "appointment_booking"),
        ("Doctor ka appointment book karna hai kal ke liye.", "appointment_booking"),

        # appointment_rescheduling
        ("Doctor cancelled my appointment, how do I reschedule?", "appointment_rescheduling"),
        ("Reschedule my clinic consultation to next Monday afternoon.", "appointment_rescheduling"),
        ("Doctor appointment reschedule karna hai kal ke liye.", "appointment_rescheduling"),

        # doctor_availability
        ("What is the qualification and OPD timing of Dr. Sharma?", "doctor_availability"),
        ("Which specialist doctor is available in evening OPD today?", "doctor_availability"),
        ("Doctor ki availability aur OPD timing kya hai?", "doctor_availability"),

        # prescription
        ("How many times a day should I take this antibiotic tablet?", "prescription"),
        ("Query regarding dosage instructions on my digital prescription.", "prescription"),
        ("Dawai kitni baar leni hai prescription me clear nahi hai.", "prescription"),

        # test_report
        ("When will my blood test lab report be available online?", "test_report"),
        ("Unable to download MRI scan radiologist report from app.", "test_report"),
        ("Lab test report abhi tak download nahi ho raha portal se.", "test_report"),

        # billing_issue / insurance_issue / other
        ("Hospital admission room billing discrepancy and itemized bill.", "billing_issue"),
        ("Is cashless hospitalization available with Star Health insurance?", "insurance_issue"),
        ("Vaccination schedule and general hospital clinic inquiry.", "other")
    ]

    # TELECOM INTENTS
    intents["telecom"] = [
        # recharge_not_updated
        ("I recharged my number but the recharge has not been updated.", "recharge_not_updated"),
        ("Bhai maine ₹599 ka recharge kiya tha. Payment successful hai aur bank se paise deduct ho gaye, lekin telecom account mein recharge update nahi hua. Purana plan hi show ho raha hai.", "recharge_not_updated"),
        ("₹599 recharge successful hai but account me new plan update nahi hua.", "recharge_not_updated"),
        ("I recharged for 365 days pack but validity not updated on SIM.", "recharge_not_updated"),
        ("Plan validity date still shows yesterday after successful recharge.", "recharge_not_updated"),
        ("Recharge validity portal par update nahi hui.", "recharge_not_updated"),

        # recharge_failed
        ("Recharge failed but money deducted from bank account.", "recharge_failed"),
        ("Payment failed during mobile recharge transaction.", "recharge_failed"),
        ("bhai mera recharge nahi hua paisa cut gaya", "recharge_failed"),

        # recharge_pending
        ("Recharge transaction is showing pending status since morning.", "recharge_pending"),
        ("Mobile recharge processing state me atka hua hai.", "recharge_pending"),

        # recharge_refund_not_received
        ("Failed recharge refund not credited back to bank account.", "recharge_refund_not_received"),
        ("Where is my refund for the cancelled mobile recharge?", "recharge_refund_not_received"),

        # wrong_recharge
        ("Recharge done on wrong mobile number by mistake.", "wrong_recharge"),
        ("Galat number par recharge ho gaya reverse kaise karein?", "wrong_recharge"),

        # plan_activation_issue
        ("I recharged for 299 pack but unlimited 5G is inactive.", "plan_activation_issue"),
        ("Recharge ho gaya par pack activate nahi hua.", "plan_activation_issue"),

        # data_not_working
        ("My mobile data is not working.", "data_not_working"),
        ("My mobile data isn't working.", "data_not_working"),
        ("My recharge succeeded but mobile data is not working.", "data_not_working"),
        ("Internet data bilkul nahi chal raha speed slow hai.", "data_not_working"),

        # call_issue / sms_issue / network_issue / sim_issue / number_porting / billing_issue / other
        ("Outgoing and incoming calls are disconnecting automatically.", "call_issue"),
        ("SMS service not working, unable to send bank verification SMS.", "sms_issue"),
        ("No network signal on my SIM card since morning.", "network_issue"),
        ("Frequent call drops in my area, please fix network tower.", "network_issue"),
        ("eSIM activation QR code expired before scanning.", "sim_issue"),
        ("How to port my mobile number to another telecom provider?", "number_porting"),
        ("Postpaid bill amount is showing extra hidden roaming charges.", "billing_issue"),
        ("What are the best prepaid international roaming plans?", "other")
    ]

    # ECOMMERCE INTENTS
    intents["ecommerce"] = [
        ("Where is my order ORD-99214 right now?", "order_delayed"),
        ("My order ORD12345 has not arrived yet and it was supposed to be delivered yesterday.", "order_delayed"),
        ("Package delivery delayed by 4 days without update.", "order_delayed"),
        ("Delivery date keeps getting pushed back.", "order_delayed"),
        ("Courier tracking link showing parcel not received.", "order_not_received"),
        ("Order marked delivered but I never received package.", "order_not_received"),
        ("Shipment tracking says delivered but security didn't receive.", "order_not_received"),
        ("I received a damaged phone in my delivery package.", "damaged_product"),
        ("Parcel box was torn and items were broken inside.", "damaged_product"),
        ("Broken glass item delivered in shipment.", "damaged_product"),
        ("Wrong color jacket delivered, need replacement immediately.", "wrong_product"),
        ("Galat item deliver ho gaya parcel me replacement do.", "wrong_product"),
        ("Received different model shoes than ordered.", "wrong_product"),
        ("Product missing from my shipment box.", "missing_item"),
        ("One item missing from multi-item order box.", "missing_item"),
        ("I want to return this dress because size is small.", "return_request"),
        ("How to book a return pickup for defective headphones?", "return_request"),
        ("Return policy window for unworn apparel.", "return_request"),
        ("Refund not credited after returning the product 5 days ago.", "refund_not_received"),
        ("Return refund amount pending in bank account.", "refund_not_received"),
        ("Please cancel my order ORD-8812.", "cancellation"),
        ("Order cancel karke refund process karein.", "cancellation"),
        ("Payment debited twice for single ecommerce order checkout.", "payment_issue"),
        ("Payment gateway debited amount but order not placed.", "payment_issue"),
        ("Courier delivery boy asked for extra money on prepaid order.", "delivery_issue"),
        ("Delivery agent refused doorstep delivery.", "delivery_issue"),
        ("What are the upcoming festive sale discounts?", "other"),
        ("Customer loyalty points and reward coupon balance.", "other")
    ]

    # INSURANCE INTENTS
    intents["insurance"] = [
        ("I want to buy a new comprehensive health insurance policy.", "policy_purchase"),
        ("How to purchase term life insurance coverage online?", "policy_purchase"),
        ("Buy motor car insurance policy quote comparison.", "policy_purchase"),
        ("How to renew my two-wheeler insurance policy online?", "policy_renewal"),
        ("Policy renewal anniversary date query.", "policy_renewal"),
        ("When will my insurance expire?", "policy_renewal"),
        ("Health policy renewal premium payment link.", "policy_renewal"),
        ("I want to check status of my health insurance claim CLM-9921.", "claim_status"),
        ("Mera claim number CLM-45612 hai, update batayein.", "claim_status"),
        ("Hospitalization cashless claim approval tracking.", "claim_status"),
        ("My car accident insurance claim was rejected unfairly.", "claim_rejected"),
        ("Claim rejected due to pre-existing disease clause dispute.", "claim_rejected"),
        ("Why was my mediclaim rejected by TPA desk?", "claim_rejected"),
        ("How to pay insurance premium installment via Net Banking?", "premium_payment"),
        ("Premium payment grace period query for term plan.", "premium_payment"),
        ("Download soft copy of my active health insurance policy document.", "policy_document"),
        ("Email digital certificate and policy bond PDF.", "policy_document"),
        ("Claim reimbursement amount credited is less than approved sum.", "reimbursement"),
        ("Reimbursement claim form submission guidelines.", "reimbursement"),
        ("What are cashless network hospitals for Star Health in Delhi?", "other"),
        ("Add family member to existing floater policy.", "other")
    ]

    # TRAVEL INTENTS
    intents["travel"] = [
        ("I want to book a one-way flight from Delhi to Mumbai.", "booking"),
        ("How to confirm hotel room reservation for 3 nights?", "booking"),
        ("Flight ticket booking karni hai kal ke liye.", "booking"),
        ("Book train ticket under tatkal quota.", "booking"),
        ("Please cancel my flight ticket reservation PNR-8812.", "cancellation"),
        ("Flight ticket cancel karna hai refund ke sath.", "cancellation"),
        ("Cancel hotel room booking with zero cancellation charges.", "cancellation"),
        ("I want to reschedule my flight departure date to next Monday.", "rescheduling"),
        ("Flight date change fee and rescheduling charges.", "rescheduling"),
        ("Reschedule hotel check-in date to next weekend.", "rescheduling"),
        ("Where is my flight cancellation refund?", "refund"),
        ("My flight was cancelled and I need a refund.", "refund"),
        ("My flight 6E-412 was cancelled, need full refund.", "refund"),
        ("Refund status for train ticket cancellation PNR.", "refund"),
        ("My flight AI-102 is delayed by 4 hours, what to do?", "flight_issue"),
        ("Flight departure terminal changed at airport gate.", "flight_issue"),
        ("Hotel refused check-in saying booking is not found.", "hotel_problem"),
        ("Hotel room air conditioning and cleanliness complaint.", "hotel_problem"),
        ("Luggage bag did not arrive at destination airport.", "baggage"),
        ("Baggage missing from luggage carousel at baggage claim.", "baggage"),
        ("Seat selection charges deducted twice during web check-in.", "payment_issue"),
        ("Flight booking payment debited but ticket not generated.", "payment_issue"),
        ("What is the free cabin baggage weight limit per passenger?", "other"),
        ("In-flight meal selection and wheelchair assistance request.", "other")
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

    ("What is the status of my order ORD-1234?", "neutral"),
    ("I want to know the last date for fee payment.", "neutral"),
    ("Please tell me the balance in my savings account.", "neutral"),
    ("How do I update my registered mobile number?", "neutral"),
    ("What are the timings for hospital OPD tomorrow?", "neutral"),
    ("Check the policy renewal amount for motor insurance.", "neutral"),
    ("Can I change my flight departure time?", "neutral"),
    ("Provide the IFSC code for Bangalore central branch.", "neutral"),
    ("When will my insurance expire?", "neutral"),
    ("Kaise bhai school ka kya fee h?", "neutral"),
    ("I paid my school fees.", "neutral"),
    ("Where is the hospital located?", "neutral"),

    ("My order has not arrived yet, and it is 3 days late.", "negative"),
    ("My UPI transaction failed and money was deducted.", "negative"),
    ("My flight was cancelled and I am stranded at the airport.", "negative"),
    ("I have contacted support three times already! Nobody is helping me. This is extremely frustrating.", "negative"),
    ("This is completely unacceptable service, solve it immediately.", "negative"),
    ("My insurance claim was rejected without any valid explanation.", "negative"),
    ("I recharged my number but the recharge has not been updated.", "negative"),
    ("I paid my college fee yesterday but it is still showing unpaid.", "negative"),
    ("Money debited twice for one ticket, refund my money now!", "negative"),
    ("No network signal since morning, this is pathetic service.", "negative"),
    ("Terrible experience, you guys are wasting my time repeatedly.", "negative"),
    ("Worst customer support ever, nobody cares about customers.", "negative"),
    ("I don't want to talk to a bot. Please connect me to a human.", "negative"),
    ("I am extremely angry with this delay!", "negative"),
    ("Bhai mere family member ki condition suddenly serious ho gayi hai aur humein urgently hospital le jaana hai.", "negative"),
    ("Maine aaj subah apni college fees ₹85,000 pay ki dono transactions deduct ho gaye total ₹1,70,000 chala gaya.", "negative"),
    ("₹75,000 do baar debit ho gaya merchant ko ek hi payment mili refund pending.", "negative")
]

# 4. EMOTION DATASET
EMOTION_DATA = [
    ("Thank you so much for resolving my refund so quickly!", "happy"),
    ("Great service, very pleased with the assistance!", "happy"),
    ("I am satisfied with the resolution provided.", "satisfied"),
    ("Everything works as expected now, thanks!", "satisfied"),
    ("What are the operational hours for bank branch?", "neutral"),
    ("Please share the syllabus for semester entrance.", "neutral"),
    ("Check the flight schedule for tomorrow morning.", "neutral"),
    ("My transaction failed, is my money safe in bank?", "concerned"),
    ("Doctor appointment pending and patient condition is serious.", "concerned"),
    ("₹85,000 college fee debited twice, need urgent reversal.", "concerned"),
    ("Family member serious hospital le jaana hai emergency appointment pending.", "fearful"),
    ("Chest pain emergency patient unconscious need immediate ambulance.", "fearful"),
    ("I am very worried about my lost baggage at airport.", "anxious"),
    ("Will my admission be cancelled if fee is delayed today?", "anxious"),
    ("I have contacted support 3 times already and nobody helped!", "frustrated"),
    ("Recharge done 5 hours ago but still not updated, ridiculous!", "frustrated"),
    ("This is the worst service ever, I am going to sue you!", "angry"),
    ("Completely unacceptable fraud, refund my money immediately!", "angry")
]

# 5. URGENCY DATASET
URGENCY_DATA = [
    ("General information regarding course brochure and fee structure.", "low"),
    ("What is the interest rate on 5 year fixed deposit?", "low"),
    ("What are the cabin baggage limits for domestic flights?", "low"),
    ("How do I update email address in my user profile?", "low"),
    ("Check order delivery tracking update for next week.", "medium"),
    ("Reschedule doctor routine checkup appointment to Friday.", "medium"),
    ("Where can I download my previous year tax payment certificate?", "medium"),
    ("My recharge pack is expiring tomorrow evening.", "medium"),
    ("₹85,000 college fee deducted twice and today is last date to submit.", "high"),
    ("₹75,000 duplicate debit from bank account need immediate reversal.", "high"),
    ("UPI transfer failed and rent deadline is in 2 hours.", "high"),
    ("Flight departs in 3 hours and boarding pass not generated.", "high"),
    ("Patient is in critical condition need emergency hospital admission now.", "critical"),
    ("Heart attack patient in ambulance need immediate emergency doctor.", "critical"),
    ("Severe medical emergency family member unconscious.", "critical"),
    ("Bank account hacked unauthorized fund transfers occurring right now.", "critical")
]

# 6. LANGUAGE DATASET
LANGUAGE_DATA = [
    ("Where is my order tracking details?", "en"),
    ("I paid my college fees yesterday online.", "en"),
    ("Please cancel my appointment with the doctor.", "en"),
    ("My flight was cancelled and I need a refund.", "en"),
    ("mera recharge abhi tak kyu nahi hua?", "hinglish"),
    ("bhai mera paisa cut gaya par payment fail ho gaya", "hinglish"),
    ("doctor ki emergency slot available hai ya nahi?", "hinglish"),
    ("college portal par fee unpaid dikha raha hai", "hinglish"),
    ("Maine subah ₹85,000 pay kiya tha do baar deduct ho gaya.", "hinglish"),
    ("₹75,000 do baar debit hua but receiver ko nahi mila.", "hinglish"),
    ("Mera UPI fail ho gaya paisa kat gaya.", "hinglish"),
    ("मेरी फीस अभी तक पोर्टल पर अपडेट नहीं हुई है।", "hi"),
    ("कृपया मेरा डॉक्टर का अपॉइंटमेंट रद्द करें।", "hi"),
    ("बैंक खाते से पैसे कट गए लेकिन भुगतान असफल रहा।", "hi"),
    ("मेरी फ्लाइट रद्द हो गई है और मुझे रिफंड चाहिए।", "hi")
]


def export_csv(data, filename):
    filepath = os.path.join(DATA_DIR, filename)
    df = pd.DataFrame(data, columns=["text", "label"])
    df.drop_duplicates(inplace=True)
    df.to_csv(filepath, index=False, encoding="utf-8")
    print(f"Exported {len(df)} rows to {filepath}")
    return filepath


def main():
    print("Generating comprehensive multi-domain NLP datasets...")
    os.makedirs(DATA_DIR, exist_ok=True)

    export_csv(DOMAIN_DATA, "domain.csv")
    for domain, intents in INTENTS_DATA.items():
        export_csv(intents, f"intent_{domain}.csv")

    export_csv(SENTIMENT_DATA, "sentiment.csv")
    export_csv(EMOTION_DATA, "emotion.csv")
    export_csv(URGENCY_DATA, "urgency.csv")
    export_csv(LANGUAGE_DATA, "language.csv")

    metadata = {
        "generated_at": pd.Timestamp.now().isoformat(),
        "domains": list(INTENTS_DATA.keys()),
        "total_domain_samples": len(DOMAIN_DATA),
        "intents_count": {d: len(intents) for d, intents in INTENTS_DATA.items()}
    }

    meta_file = os.path.join(METADATA_DIR, "dataset_info.json")
    with open(meta_file, "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2)

    print(f"Saved dataset metadata to {meta_file}")


if __name__ == "__main__":
    main()
