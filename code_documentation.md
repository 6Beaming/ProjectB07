# Code Documentation  
_for: B07 Safety Planning App (Bright Sky)_

---

## 1. Introduction

This Android application is designed to help users in abusive relationships build and manage a personalized safety plan. Core features include a dynamic questionnaire, secure authentication (including PIN setup), emergency exit, structured storage for emergency information (contacts, documents, safe locations, medications), and quick access to local support resources. All sensitive data is managed via Firebase Authentication, Realtime Database, and Storage.

---

## 2. Project Structure

**Main package:** `com.group15.b07project`

- **Activities:**  
  `AuthChoiceActivity.java`, `DisclaimerActivity.java`, `LaunchActivity.java`, `LoginActivity.java`, `MainActivity.java`, `PinLoginActivity.java`, `PinSetupActivity.java`, `RegisterActivity.java`, `ResetActivity.java`, `SignupActivity.java`
- **Fragments:**  
  `DocumentsToPackFragment.java`, `EditDocumentFragment.java`, `EmergencyContactsFragment.java`, `HomeFragment.java`, `MedicationsFragment.java`, `PlanGenerationFragment.java`, `QuestionnaireFragment.java`, `SafeLocationsFragment.java`, `StorageOfEmergencyInfoFragment.java`, `SupportConnectionFragment.java`, `AddContactFragment.java`, `AddDocumentFragment.java`, `AddLocationFragment.java`, `AddMedicationFragment.java`
- **Adapters:**  
  `DocumentAdapter.java`, `EmergencyContactsAdapter.java`, `MedicationsAdapter.java`, `SafeLocationsAdapter.java`, `ServiceAdapter.java`, `TipAdapter.java`
- **Models/Data Structures:**  
  `DocMetadataStructure.java`, `DocsDataStructure.java`, `EmergencyContact.java`, `Medication.java`, `Question.java`, `QuestionsBundle.java`, `SafeLocation.java`, `ServiceDirectory.java`, `ServiceEntry.java`
- **Helpers/Utils:**  
  `FirebaseFileHelper.java`, `ParseJson.java`, `PinManager.java`, `SimpleTextWatcher.java`
- **Login MVP:**  
  `LoginContract.java`, `LoginModel.java`, `LoginPresenter.java`

---

## 3. Class and File Responsibilities

### Activities

- **AuthChoiceActivity.java**  
  Entry screen to choose between login or registration.

- **DisclaimerActivity.java**  
  Shows app disclaimers and user agreements.

- **LaunchActivity.java**  
  Initial launcher activity, likely handles splash or setup logic.

- **LoginActivity.java**  
  Handles user authentication via Firebase (email/password). Manages login process, input validation, and error display.

- **RegisterActivity.java**  
  Manages new user registration with Firebase Authentication.

- **SignupActivity.java**  
  May serve as an alternate entry point or legacy registration.

- **MainActivity.java**  
  Root activity hosting app navigation. Manages fragment switching for the main app features (questionnaire, plan generation, emergency info, support resources).

- **PinSetupActivity.java**  
  After registration, prompts user to set a secure PIN. Utilizes `PinManager.java` for encryption/storage.

- **PinLoginActivity.java**  
  Handles PIN authentication at app entry, uses `PinManager.java` to verify.

- **ResetActivity.java**  
  Allows users to reset their password or PIN.

---

### Fragments

- **HomeFragment.java**  
  App dashboard, possibly showing navigation to all primary modules.

- **QuestionnaireFragment.java**  
  Loads safety planning questions from JSON (via `ParseJson.java` and `QuestionsBundle.java`). Presents multi-step questionnaire UI. Saves answers to Firebase Realtime Database.

- **PlanGenerationFragment.java**  
  Displays the generated safety plan based on user answers in the questionnaire. Shows personalized safety advice.

- **StorageOfEmergencyInfoFragment.java**  
  Hosts tabs or navigation for emergency contacts, documents, medications, and safe locations.

- **EmergencyContactsFragment.java**  
  Displays and manages a list of user-defined emergency contacts. Integrates `EmergencyContactsAdapter.java`. Supports adding/editing contacts (via `AddContactFragment.java`). Data is persisted to Firebase.

- **EditDocumentFragment.java**  
  Allows viewing/editing metadata for an emergency document.

- **DocumentsToPackFragment.java**  
  Lists important documents the user should consider preparing. 

- **MedicationsFragment.java**  
  Displays and manages the user's list of medications. Integrates `MedicationsAdapter.java`. Supports adding/editing medications (via `AddMedicationFragment.java`). Data is synced to Firebase.

- **SafeLocationsFragment.java**  
  Manages user's safe locations. Uses `SafeLocationsAdapter.java` for the list and `AddLocationFragment.java` for data entry.

- **SupportConnectionFragment.java**  
  Displays local support resources by city. Loads data using `ServiceDirectory.java` and displays via `ServiceAdapter.java`.

- **AddContactFragment.java**, **AddDocumentFragment.java**, **AddLocationFragment.java**, **AddMedicationFragment.java**  
  Modular fragments for creating or editing emergency info items.

---

### Adapters

- **DocumentAdapter.java**  
  RecyclerView adapter for displaying the list of emergency documents and their metadata.

- **EmergencyContactsAdapter.java**  
  Adapter for the contacts list in `EmergencyContactsFragment.java`.

- **MedicationsAdapter.java**  
  Adapter for displaying the user's medications.

- **SafeLocationsAdapter.java**  
  Adapter for user's safe locations.

- **ServiceAdapter.java**  
  Adapter for the support services/resource list.

- **TipAdapter.java**  
  Adapter for displaying safety plan tips in plan generation.

---

### Models / Data Structures

- **DocMetadataStructure.java** / **DocsDataStructure.java**  
  POJOs for storing and handling document metadata and data.

- **EmergencyContact.java**  
  Represents a single emergency contact.

- **Medication.java**  
  Represents a single medication item.

- **SafeLocation.java**  
  POJO for user's safe location.

- **Question.java**, **QuestionsBundle.java**  
  Represent questionnaire questions and their bundles.

- **ServiceDirectory.java**, **ServiceEntry.java**  
  Classes for support resource directory and entries.

---

### Utilities / Helpers

- **FirebaseFileHelper.java**  
  Handles file upload and download to/from Firebase Storage for documents.

- **ParseJson.java**  
  Utility for loading and parsing JSON configuration/resources.

- **PinManager.java**  
  Handles PIN encryption, storage, and validation (secure local storage).

- **SimpleTextWatcher.java**  
  Convenience class for handling EditText input changes.

---

### Login MVP

- **LoginContract.java**, **LoginModel.java**, **LoginPresenter.java**  
  Implements Model-View-Presenter pattern for login UI logic.

---

## 4. Data Flow and Firebase Structure

- **All user-specific data** is stored in Firebase under `/users/{uid}/...`
  - `/plan/answers` - Questionnaire responses
  - `/emergency_contacts` - Contacts
  - `/documents` - Document metadata
  - `/safe_locations` - Safe location list
  - `/medications` - Medication list
  - `/city` - User's chosen city for support resources
- **Files** (PDFs/images) are uploaded via `FirebaseFileHelper.java` to Firebase Storage under `/documents/{uid}/...`

**Typical workflow:**
1. User registers/logs in (`LoginActivity.java`/`RegisterActivity.java`), sets PIN (`PinSetupActivity.java`)
2. Navigates app from `MainActivity.java`/`HomeFragment.java`
3. Completes questionnaire (`QuestionnaireFragment.java`), views safety plan (`PlanGenerationFragment.java`)
4. Adds/edits emergency info (contacts, docs, locations, medications) via their fragments
5. Accesses support resources by city
6. Can use emergency exit (implemented in navigation logic)

---

## 5. Running and Testing

- Open in Android Studio, configure Firebase (`google-services.json`)
- Run `MainActivity.java`
- Test:
  - Registration, login, PIN flows
  - Questionnaire and safety plan
  - Emergency contacts, documents, safe locations, medications (add, edit, delete)
  - File upload/download
  - Support connection resources

---

## 6. Coding Practices

- Each major feature in its own Fragment and Adapter for maintainability
- Models as POJOs, clear separation of data and UI logic
- MVP for login for testability
- All sensitive info encrypted or securely stored
- Inline comments where needed for nontrivial logic

---

## 7. Appendix: File/Responsibility Table (Quick Reference)

| File/Class Name                  | Responsibility                                             |
|----------------------------------|-----------------------------------------------------------|
| AuthChoiceActivity.java          | Login/register entrypoint                                 |
| DisclaimerActivity.java          | App disclaimer/legal info                                 |
| LaunchActivity.java              | Initial launch/splash logic                               |
| LoginActivity.java               | Firebase login logic                                      |
| RegisterActivity.java            | User registration logic                                   |
| SignupActivity.java              | Alternate/legacy registration                             |
| MainActivity.java                | Root activity, fragment navigation                        |
| PinSetupActivity.java            | User PIN setup                                            |
| PinLoginActivity.java            | PIN entry and verification                                |
| ResetActivity.java               | Reset password/PIN flows                                  |
| HomeFragment.java                | Dashboard/navigation                                      |
| QuestionnaireFragment.java       | Dynamic questionnaire UI, answer submission               |
| PlanGenerationFragment.java      | Safety plan display                                       |
| StorageOfEmergencyInfoFragment.java | Emergency info (contacts/docs/locations/meds) host    |
| EmergencyContactsFragment.java   | Manage emergency contacts                                 |
| EditDocumentFragment.java        | Edit/view doc metadata                                    |
| DocumentsToPackFragment.java     | List docs to pack                                         |
| MedicationsFragment.java         | Manage medications                                        |
| SafeLocationsFragment.java       | Manage safe locations                                     |
| SupportConnectionFragment.java   | Support resources by city                                 |
| AddContactFragment.java          | Add or edit contact information                           |
| AddDocumentFragment.java         | Add or edit document information                          |
| AddLocationFragment.java         | Add or edit safe location information                     |
| AddMedicationFragment.java       | Add or edit medication information                        |
| DocumentAdapter.java             | RecyclerView adapter for document list                    |
| EmergencyContactsAdapter.java    | RecyclerView adapter for contacts list                    |
| MedicationsAdapter.java          | RecyclerView adapter for medication list                  |
| SafeLocationsAdapter.java        | RecyclerView adapter for safe location list               |
| ServiceAdapter.java              | RecyclerView adapter for support service list             |
| TipAdapter.java                  | RecyclerView adapter for safety tips                      |
| DocMetadataStructure.java        | POJO for document metadata                                |
| DocsDataStructure.java           | POJO for document data                                    |
| EmergencyContact.java            | POJO for contact information                              |
| Medication.java                  | POJO for medication information                           |
| SafeLocation.java                | POJO for safe location information                        |
| Question.java                    | POJO for questionnaire question                           |
| QuestionsBundle.java             | POJO for a bundle of questions                            |
| ServiceDirectory.java            | Support resource directory loader                         |
| ServiceEntry.java                | POJO for a support service entry                          |
| FirebaseFileHelper.java          | Firebase Storage file helper                              |
| ParseJson.java                   | JSON resource loader                                      |
| PinManager.java                  | PIN security (encryption, storage, verification)          |
| SimpleTextWatcher.java           | EditText input helper                                     |
| LoginContract.java               | Login MVP architecture interface                          |
| LoginModel.java                  | Login MVP model                                           |
| LoginPresenter.java              | Login MVP presenter                                       |

---
