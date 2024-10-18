package com.community.api.services;

import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomProductState;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.Districts;
import com.community.api.entity.Qualification;
import com.community.api.entity.Role;
import com.community.api.entity.ServiceProviderAddressRef;
import com.community.api.entity.ServiceProviderInfra;
import com.community.api.entity.ServiceProviderLanguage;
import com.community.api.entity.Skill;
import com.community.api.entity.StateCode;
import com.community.api.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.broadleafcommerce.common.util.sql.importsql.DemoSqlServerSingleLineSqlCommandExtractor.CURRENT_TIMESTAMP;

@Component
public class CommandLineService implements CommandLineRunner {

    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Check if data already exists to avoid duplication
        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomProductState c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomProductState(1L, "NEW", "New State."));
            entityManager.persist(new CustomProductState(2L, "MODIFIED", "Modified State."));
            entityManager.persist(new CustomProductState(3L, "APPROVED", "Approved State."));
            entityManager.persist(new CustomProductState(4L, "REJECTED", "Rejected State."));
            entityManager.persist(new CustomProductState(5L, "LIVE", "Live State."));
            entityManager.persist(new CustomProductState(6L, "EXPIRED", "Expired State."));
        }

        if(entityManager.createQuery("SELECT COUNT(c) FROM CustomJobGroup c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomJobGroup(1L, 'A', "Executive Management"));
            entityManager.persist(new CustomJobGroup(2L, 'B', "Professional and Technical"));
            entityManager.persist(new CustomJobGroup(3L, 'C', "Administrative and Support"));
            entityManager.persist(new CustomJobGroup(4L, 'D', "Entry-Level and Labor"));
        }

        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomApplicationScope c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomApplicationScope(1L, "STATE", "State level operations."));
            entityManager.persist(new CustomApplicationScope(2L, "CENTER", "Center level operations."));
        }

        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomReserveCategory c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomReserveCategory(1L, "GEN", "General", true));
            entityManager.persist(new CustomReserveCategory(2L, "SC", "Schedule Caste", false));
            entityManager.persist(new CustomReserveCategory(3L, "ST", "Schedule Tribe", false));
            entityManager.persist(new CustomReserveCategory(4L, "OBC", "Other Backward Caste", false));
            entityManager.persist(new CustomReserveCategory(5L, "OTHERS", "Others", false));
        }

        if(entityManager.createQuery("SELECT COUNT(c) FROM CustomProductRejectionStatus c", Long.class).getSingleResult() == 0) {
            entityManager.merge(new CustomProductRejectionStatus(1L, "TO-BE-MODIFIED", "Product needs modification to get approved."));
            entityManager.merge(new CustomProductRejectionStatus(2L, "DUPLICATE", "There is already a product present with these details."));
            entityManager.merge(new CustomProductRejectionStatus(3L, "IRRELEVANT", "The product is irrelevant."));
            entityManager.merge(new CustomProductRejectionStatus(4L, "UNFEASIBLE", "The product is not feasible to exists."));
        }

        if(entityManager.createQuery("SELECT COUNT(c) FROM CustomGender c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomGender(1L, 'M', "MALE"));
            entityManager.persist(new CustomGender(2L, 'F', "FEMALE"));
            entityManager.persist(new CustomGender(3L, 'O', "OTHERS"));
        }

        if(entityManager.createQuery("SELECT COUNT(c) FROM CustomSector c", Long.class).getSingleResult() == 0) {
            entityManager.merge(new CustomSector(1L, "HEALTHCARE", "Forms related to patient care and medical services."));
            entityManager.merge(new CustomSector(2L, "EDUCATION", "Forms for student enrollment and academic records."));
            entityManager.merge(new CustomSector(3L, "FINANCE", "Forms for loans, taxes, and financial services."));
            entityManager.merge(new CustomSector(4L, "GOVERNMENT", "Forms for taxes and civic registration."));
            entityManager.merge(new CustomSector(5L, "HUMAN_RESOURCES", "Forms for job applications and employee management."));
            entityManager.merge(new CustomSector(6L, "REAL_ESTATE", "Forms for property transactions and leases."));
            entityManager.merge(new CustomSector(7L, "INSURANCE", "Forms for claims and policy management."));
            entityManager.merge(new CustomSector(8L, "RETAIL", "Forms for customer feedback and warranties."));
            entityManager.merge(new CustomSector(9L, "TRANSPORTATION", "Forms for shipping and travel documentation."));
            entityManager.merge(new CustomSector(10L, "LEGAL", "Forms for legal processes and documentation."));
        }

        if(entityManager.createQuery("SELECT COUNT(c) FROM CustomStream c", Long.class).getSingleResult() == 0) {
            entityManager.merge(new CustomStream(1L, "SCIENCE", "Description of Science"));
            entityManager.merge(new CustomStream(2L, "ARTS", "Description of Arts"));
            entityManager.merge(new CustomStream(3L, "COMMERCE", "Description of Commerce"));
            entityManager.merge(new CustomStream(4L, "ENGINEERING", "Description of Engineering"));
            entityManager.merge(new CustomStream(5L, "MEDICINE", "Description of Medicine"));
            entityManager.merge(new CustomStream(6L, "HUMANITIES", "Description of Humanities"));
            entityManager.merge(new CustomStream(7L, "SOCIAL SCIENCES", "Description of Social Sciences"));
            entityManager.merge(new CustomStream(8L, "TECHNOLOGY", "Description of Technology"));
            entityManager.merge(new CustomStream(9L, "MATHEMATICS", "Description of Mathematics"));
            entityManager.merge(new CustomStream(10L, "DESIGN", "Description of Design"));
        }

        if(entityManager.createQuery("SELECT COUNT(s) FROM CustomSubject s", Long.class).getSingleResult() == 0) {
            entityManager.merge(new CustomSubject(1L, "Mathematics", "Description of Mathematics"));
            entityManager.merge(new CustomSubject(2L, "Physics", "Description of Physics"));
            entityManager.merge(new CustomSubject(3L, "Chemistry", "Description of Chemistry"));
            entityManager.merge(new CustomSubject(4L, "Biology", "Description of Biology"));
            entityManager.merge(new CustomSubject(5L, "English", "Description of English"));
            entityManager.merge(new CustomSubject(6L, "History", "Description of History"));
            entityManager.merge(new CustomSubject(7L, "Geography", "Description of Geography"));
            entityManager.merge(new CustomSubject(8L, "Computer Science", "Description of Computer Science"));
            entityManager.merge(new CustomSubject(9L, "Art", "Description of Art"));
            entityManager.merge(new CustomSubject(10L, "Physical Education", "Description of Physical Education"));
        }

        if (entityManager.createQuery("SELECT COUNT(r) FROM Role r", Long.class).getSingleResult() == 0) {
            entityManager.merge(new Role(1, "SUPER_ADMIN", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "SUPER_ADMIN"));
            entityManager.merge(new Role(2, "ADMIN", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "SUPER_ADMIN"));
            entityManager.merge(new Role(3, "ADMIN_SERVICE_PROVIDER", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "SUPER_ADMIN"));
            entityManager.merge(new Role(4, "SERVICE_PROVIDER", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "SUPER_ADMIN"));
            entityManager.merge(new Role(5, "CUSTOMER", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "SUPER_ADMIN"));
        }

        Long count = entityManager.createQuery("SELECT COUNT(d) FROM Districts d", Long.class).getSingleResult();

        if (count == 0) {
            // Insert data into the Districts table with explicit ids
            entityManager.persist(new Districts(1, "Bilaspur", "HP"));
            entityManager.persist(new Districts(2, "Chamba", "HP"));
            entityManager.persist(new Districts(3, "Hamirpur", "HP"));
            entityManager.persist(new Districts(4, "Kangra", "HP"));
            entityManager.persist(new Districts(5, "Kinnaur", "HP"));
            entityManager.persist(new Districts(6, "Kullu", "HP"));
            entityManager.persist(new Districts(7, "Lahaul and Spiti", "HP"));
            entityManager.persist(new Districts(8, "Mandi", "HP"));
            entityManager.persist(new Districts(9, "Shimla", "HP"));
            entityManager.persist(new Districts(10, "Sirmaur", "HP"));
            entityManager.persist(new Districts(11, "Solan", "HP"));
            entityManager.persist(new Districts(12, "Una", "HP"));

            // Jammu Division
            entityManager.persist(new Districts(13, "Jammu", "JK"));
            entityManager.persist(new Districts(14, "Samba", "JK"));
            entityManager.persist(new Districts(15, "Kathua", "JK"));
            entityManager.persist(new Districts(16, "Udhampur", "JK"));
            entityManager.persist(new Districts(17, "Reasi", "JK"));
            entityManager.persist(new Districts(18, "Ramban", "JK"));
            entityManager.persist(new Districts(19, "Doda", "JK"));
            entityManager.persist(new Districts(20, "Poonch", "JK"));
            entityManager.persist(new Districts(21, "Rajouri", "JK"));
            entityManager.persist(new Districts(22, "Anantnag", "JK"));
            entityManager.persist(new Districts(23, "Kishtwar", "JK"));

            // Kashmir Division
            entityManager.persist(new Districts(24, "Srinagar", "JK"));
            entityManager.persist(new Districts(25, "Baramulla", "JK"));
            entityManager.persist(new Districts(26, "Pulwama", "JK"));
            entityManager.persist(new Districts(27, "Shopian", "JK"));
            entityManager.persist(new Districts(28, "Anantnag", "JK"));
            entityManager.persist(new Districts(29, "Bandipora", "JK"));
            entityManager.persist(new Districts(30, "Ganderbal", "JK"));
            entityManager.persist(new Districts(31, "Kulgam", "JK"));

            // Punjab
            entityManager.persist(new Districts(32, "Amritsar", "PB"));
            entityManager.persist(new Districts(33, "Barnala", "PB"));
            entityManager.persist(new Districts(34, "Bathinda", "PB"));
            entityManager.persist(new Districts(35, "Faridkot", "PB"));
            entityManager.persist(new Districts(36, "Fatehgarh Sahib", "PB"));
            entityManager.persist(new Districts(37, "Fazilka", "PB"));
            entityManager.persist(new Districts(38, "Ferozepur", "PB"));
            entityManager.persist(new Districts(39, "Gurdaspur", "PB"));
            entityManager.persist(new Districts(40, "Hoshiarpur", "PB"));
            entityManager.persist(new Districts(41, "Jalandhar", "PB"));
            entityManager.persist(new Districts(42, "Kapurthala", "PB"));
            entityManager.persist(new Districts(43, "Ludhiana", "PB"));
            entityManager.persist(new Districts(44, "Mansa", "PB"));
            entityManager.persist(new Districts(45, "Moga", "PB"));
            entityManager.persist(new Districts(46, "Mohali", "PB"));
            entityManager.persist(new Districts(47, "Pathankot", "PB"));
            entityManager.persist(new Districts(48, "Patiala", "PB"));
            entityManager.persist(new Districts(49, "Rupnagar", "PB"));
            entityManager.persist(new Districts(50, "Sangrur", "PB"));
            entityManager.persist(new Districts(51, "Tarn Taran", "PB"));

            // Haryana
            entityManager.persist(new Districts(52, "Ambala", "HR"));
            entityManager.persist(new Districts(53, "Bhiwani", "HR"));
            entityManager.persist(new Districts(54, "Faridabad", "HR"));
            entityManager.persist(new Districts(55, "Fatehabad", "HR"));
            entityManager.persist(new Districts(56, "Gurgaon", "HR"));
            entityManager.persist(new Districts(57, "Hisar", "HR"));
            entityManager.persist(new Districts(58, "Jhajjar", "HR"));
            entityManager.persist(new Districts(59, "Jind", "HR"));
            entityManager.persist(new Districts(60, "Kaithal", "HR"));
            entityManager.persist(new Districts(61, "Karnal", "HR"));
            entityManager.persist(new Districts(62, "Mahendragarh", "HR"));
            entityManager.persist(new Districts(63, "Mewat", "HR"));
            entityManager.persist(new Districts(64, "Palwal", "HR"));
            entityManager.persist(new Districts(65, "Panchkula", "HR"));
            entityManager.persist(new Districts(66, "Panipat", "HR"));
            entityManager.persist(new Districts(67, "Rewari", "HR"));
            entityManager.persist(new Districts(68, "Sirsa", "HR"));
            entityManager.persist(new Districts(69, "Sonipat", "HR"));
            entityManager.persist(new Districts(70, "Yamunanagar", "HR"));
        }


        count = entityManager.createQuery("SELECT COUNT(s) FROM StateCode s", Long.class).getSingleResult();

        if (count == 0) {

            // Insert data into the StateCode table
            entityManager.persist(new StateCode(1, "Andhra Pradesh", "AP"));
            entityManager.persist(new StateCode(2, "Arunachal Pradesh", "AR"));
            entityManager.persist(new StateCode(3, "Assam", "AS"));
            entityManager.persist(new StateCode(4, "Bihar", "BR"));
            entityManager.persist(new StateCode(5, "Chhattisgarh", "CG"));
            entityManager.persist(new StateCode(6, "Goa", "GA"));
            entityManager.persist(new StateCode(7, "Gujarat", "GJ"));
            entityManager.persist(new StateCode(8, "Haryana", "HR"));
            entityManager.persist(new StateCode(9, "Himachal Pradesh", "HP"));
            entityManager.persist(new StateCode(10, "Jharkhand", "JH"));
            entityManager.persist(new StateCode(11, "Karnataka", "KA"));
            entityManager.persist(new StateCode(12, "Kerala", "KL"));
            entityManager.persist(new StateCode(13, "Madhya Pradesh", "MP"));
            entityManager.persist(new StateCode(14, "Maharashtra", "MH"));
            entityManager.persist(new StateCode(15, "Manipur", "MN"));
            entityManager.persist(new StateCode(16, "Meghalaya", "ML"));
            entityManager.persist(new StateCode(17, "Mizoram", "MZ"));
            entityManager.persist(new StateCode(18, "Nagaland", "NL"));
            entityManager.persist(new StateCode(19, "Odisha", "OD"));
            entityManager.persist(new StateCode(20, "Punjab", "PB"));
            entityManager.persist(new StateCode(21, "Rajasthan", "RJ"));
            entityManager.persist(new StateCode(22, "Sikkim", "SK"));
            entityManager.persist(new StateCode(23, "Tamil Nadu", "TN"));
            entityManager.persist(new StateCode(24, "Telangana", "TS"));
            entityManager.persist(new StateCode(25, "Tripura", "TR"));
            entityManager.persist(new StateCode(26, "Uttar Pradesh", "UP"));
            entityManager.persist(new StateCode(27, "Uttarakhand", "UK"));
            entityManager.persist(new StateCode(28, "West Bengal", "WB"));

            // Union Territories
            entityManager.persist(new StateCode(29, "Andaman and Nicobar Islands", "AN"));
            entityManager.persist(new StateCode(30, "Chandigarh", "CH"));
            entityManager.persist(new StateCode(31, "Dadra and Nagar Haveli and Daman and Diu", "DN"));
            entityManager.persist(new StateCode(32, "Lakshadweep", "LD"));
            entityManager.persist(new StateCode(33, "Delhi", "DL"));
            entityManager.persist(new StateCode(34, "Puducherry", "PY"));
        }
        count = entityManager.createQuery("SELECT COUNT(a) FROM ServiceProviderAddressRef a", Long.class).getSingleResult();

        if (count == 0) {

            // Insert data into the ServiceProviderAddress table
            entityManager.persist(new ServiceProviderAddressRef(1, "OFFICE_ADDRESS"));
            entityManager.persist(new ServiceProviderAddressRef(2, "CURRENT_ADDRESS"));
            entityManager.persist(new ServiceProviderAddressRef(3, "BILLING_ADDRESS"));
            entityManager.persist(new ServiceProviderAddressRef(4, "MAILING_ADDRESS"));
        }
        count = entityManager.createQuery("SELECT COUNT(l) FROM ServiceProviderLanguage l", Long.class).getSingleResult();

        if (count == 0) {
            entityManager.persist(new ServiceProviderLanguage(1, "Hindi"));
            entityManager.persist(new ServiceProviderLanguage(2, "Bengali"));
            entityManager.persist(new ServiceProviderLanguage(3, "Telugu"));
            entityManager.persist(new ServiceProviderLanguage(4, "Marathi"));
            entityManager.persist(new ServiceProviderLanguage(5, "Tamil"));
            entityManager.persist(new ServiceProviderLanguage(6, "Gujarati"));
            entityManager.persist(new ServiceProviderLanguage(7, "Punjabi"));
        }

        count = entityManager.createQuery("SELECT COUNT(i) FROM ServiceProviderInfra i", Long.class).getSingleResult();

        if (count == 0) {
            entityManager.persist(new ServiceProviderInfra(1, "DESKTOP"));
            entityManager.persist(new ServiceProviderInfra(2, "SCANNER"));
            entityManager.persist(new ServiceProviderInfra(3, "LAPTOP"));
            entityManager.persist(new ServiceProviderInfra(4, "PRINTER"));
            entityManager.persist(new ServiceProviderInfra(5, "INTERNET_BROADBAND"));
        }

        count = entityManager.createQuery("SELECT COUNT(s) FROM Skill s", Long.class).getSingleResult();

        if (count == 0) {
            entityManager.persist(new Skill(1, "Form Filling Knowledge/Expertise"));
            entityManager.persist(new Skill(2, "Resizing & Uploading Image/Document"));
            entityManager.persist(new Skill(3, "Executing Online Payment/Transactions"));
            entityManager.persist(new Skill(4, "Apply To Various Government Schemes"));
        }
        count = entityManager.createQuery("SELECT COUNT(s) FROM ServiceProviderStatus s", Long.class).getSingleResult();

        if (count == 0) {
            // Get current date and time as a formatted string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String now = LocalDateTime.now().format(formatter);

            // Create new instances of ServiceProviderStatus
            ServiceProviderStatus status1 = new ServiceProviderStatus(1, "DOCUMENTS_SUBMISSION_PENDING", "Documents submission is pending", now, now, "SUPER_ADMIN");
            ServiceProviderStatus status2 = new ServiceProviderStatus(2, "APPLIED", "Application has been submitted", now, now, "SUPER_ADMIN");
            ServiceProviderStatus status3 = new ServiceProviderStatus(3, "APPROVAL_PENDING", "Application is awaiting approval", now, now, "SUPER_ADMIN");
            ServiceProviderStatus status4 = new ServiceProviderStatus(4, "APPROVED", "Application has been approved", now, now, "SUPER_ADMIN");

            // Persist the instances
            entityManager.persist(status1);
            entityManager.persist(status2);
            entityManager.persist(status3);
            entityManager.persist(status4);
        }
        count = entityManager.createQuery("SELECT COUNT(e) FROM Qualification e", Long.class).getSingleResult();

        if (count == 0) {
            entityManager.persist(new Qualification(1L, "MATRICULATION", "Completed secondary education or equivalent"));
            entityManager.persist(new Qualification(2L, "INTERMEDIATE", "Completed higher secondary education or equivalent"));
            entityManager.persist(new Qualification(3L, "BACHELORS", "Completed undergraduate degree program"));
            entityManager.persist(new Qualification(4L, "MASTERS", "Completed postgraduate degree program"));
            entityManager.persist(new Qualification(5L, "DOCTORATE", "Completed doctoral degree program"));

        }

        count = entityManager.createQuery("SELECT COUNT(e) FROM TypingText e", Long.class).getSingleResult();
        if (count == 0) {
            entityManager.merge(new TypingText(1L, "The quick brown fox jumps over the lazy dog near the quiet river, while the bright sun sets in the horizon, casting beautiful hues of orange."));
            entityManager.merge(new TypingText(2L, "A curious cat chased a butterfly through the green meadows, unaware of the gentle breeze swirling around."));
            entityManager.merge(new TypingText(3L, "In the silent night, a lone owl hooted softly as the stars twinkled brightly above the peaceful forest."));
            entityManager.merge(new TypingText(4L, "Beneath the tall mountains, a small village thrived with joy, laughter, and the warmth of togetherness."));
            entityManager.merge(new TypingText(5L, "The adventure begins with a journey through unknown lands, filled with unexpected challenges and thrilling discoveries along the way."));
        }

        count = entityManager.createQuery("SELECT count(e) FROM ServiceProviderTestStatus e", Long.class).getSingleResult();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);

        if (count == 0) {
            entityManager.persist(new ServiceProviderTestStatus(1L, "New", "The service provider has registered but has not yet completed the test.", now, now, "SUPER_ADMIN"));
            entityManager.persist(new ServiceProviderTestStatus(2L, "Completed Test", "The service provider has completed the required skill tests.", now, now, "SUPER_ADMIN"));
            entityManager.persist(new ServiceProviderTestStatus(3L, "Approved", "The service provider's submission has been reviewed and approved.", now, now, "SUPER_ADMIN"));
            entityManager.persist(new ServiceProviderTestStatus(4L, "Rejected", "The service provider's submission was rejected due to not meeting the criteria.", now, now, "SUPER_ADMIN"));
            entityManager.persist(new ServiceProviderTestStatus(5L, "Suspended", "The service provider account is currently suspended due to policy violations.", now, now, "SUPER_ADMIN"));
        }
        count = entityManager.createQuery("SELECT count(e) FROM ServiceProviderRank e", Long.class).getSingleResult();

        if (count == 0) {
            entityManager.persist(new ServiceProviderRank(1L, "1a", "The PROFESSIONAL service provider's score is between 75-100 points", now, now, "SUPER_ADMIN", 12, 50));
            entityManager.persist(new ServiceProviderRank(2L, "1b", "The PROFESSIONAL service provider's score is between 50-75 points", now, now, "SUPER_ADMIN", 6, 25));
            entityManager.persist(new ServiceProviderRank(3L, "1c", "The PROFESSIONAL service provider's score is between 25-50 points", now, now, "SUPER_ADMIN", 4,17));
            entityManager.persist(new ServiceProviderRank(4L, "1d", "The PROFESSIONAL service provider's score is between 0-25 points", now, now, "SUPER_ADMIN", 3, 13));
            entityManager.persist(new ServiceProviderRank(5L, "2a", "The INDIVIDUAL service provider's score is between 75-100 points", now, now, "SUPER_ADMIN", 6, 25));
            entityManager.persist(new ServiceProviderRank(6L, "2b", "The INDIVIDUAL service provider's score is between 50-75 points", now, now, "SUPER_ADMIN", 3, 13));
            entityManager.persist(new ServiceProviderRank(7L, "2c", "The INDIVIDUAL service provider's score is between 25-50 points", now, now, "SUPER_ADMIN", 2, 8));
            entityManager.persist(new ServiceProviderRank(8L, "2d", "The INDIVIDUAL service provider's score is between 0-25 points", now, now, "SUPER_ADMIN", 2, 6));
        }

         count = entityManager.createQuery("SELECT count(e) FROM ScoringCriteria e", Long.class).getSingleResult();

        if (count == 0) {

            // Business Unit / Infrastructure Scoring
            entityManager.merge(new ScoringCriteria(1L, "Business Unit / Infrastructure", "If it's a Business Unit: 20 points", 20));

            // Work Experience Scoring
            entityManager.merge(new ScoringCriteria(2L, "Work Experience", "1 year work experience", 5));
            entityManager.merge(new ScoringCriteria(3L, "Work Experience", "2 years work experience", 10));
            entityManager.merge(new ScoringCriteria(4L, "Work Experience", "3 years work experience", 15));
            entityManager.merge(new ScoringCriteria(5L, "Work Experience", "5 or more years work experience", 20));

            // Qualification Scoring
            entityManager.merge(new ScoringCriteria(6L, "Qualification", "Service Provider is graduated or above qualified", 10));
            entityManager.merge(new ScoringCriteria(7L, "Qualification", "Service Provider is 12th passed", 5));

            // Technical Expertise Scoring
            entityManager.merge(new ScoringCriteria(8L, "Technical Expertise", "Each skill will score 2 points", 2));
            entityManager.merge(new ScoringCriteria(9L, "Technical Expertise", "Service Provider having equal to or more than 5 skills", 10));

            // Staff Scoring
            entityManager.merge(new ScoringCriteria(10L, "Staff", "More than 4 staff members", 10));
            entityManager.merge(new ScoringCriteria(11L, "Staff", "2 staff members", 5));
            entityManager.merge(new ScoringCriteria(12L, "Staff", "Individual (no staff)", 0));

            //Infra Scoring (For individual)
            entityManager.merge(new ScoringCriteria(13L,"Infrastructure","Service Provider having Equal to 5 or more than 5 infrastructures",20));
            entityManager.merge(new ScoringCriteria(14L,"Infrastructure","Service Provider having between 2 and 4 infrastructures",10));
            entityManager.merge(new ScoringCriteria(15L,"Infrastructure","Service Provider having 1 infrastructure",5));
            entityManager.merge(new ScoringCriteria(16L,"Infrastructure","Service Provider having 0 infrastructure",0));

            //PartTimeOrFullTime Scoring (For Individual)
            entityManager.merge(new ScoringCriteria(17L,"PartTimeOrFullTime","Service Provider who is Full time",10));
            entityManager.merge(new ScoringCriteria(18L,"PartTimeOrFullTime","Service Provider who is Part time",0));
        }

    }
}
