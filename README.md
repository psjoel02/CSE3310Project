# CSE3310Project
Github link (For access to any changes in the future): https://github.com/somestudentatschool/CSE3310Project
First, either clone the repository from GitHub or open the project in Android Studio. 

Then, run the project using the run button in the top right. Make sure to have an Android emulator already installed. 

Next, after building and running the project in an emulator (or phone), you should see the login screen. If you have an account, then sign in with your username
and password. Otherwise, press the ‘sign up’ button and input your Gmail address, a username, and a password. You should receive an email for verification after
registering and a notification on your phone.  

Now, after signing in or registering, you should be at the home screen, which has buttons for the image uploader, profile page, log out, and security questions.
The profile page will show your full name, username, email, password, date of birth, and upload an image from your camera or gallery after requesting
permissions. There are options to change these on this page and changing your email or password will take you to a new page that requires entering your old
credentials. Clicking on the delete account button will display a confirmation dialog to the user and if selected permanently delete all data on the account. On
the homepage, if you click on the sign out button it will take you to the sign in screen. If you click on the change security questions button it will take you
to the security questions page and allow you to change your security questions and answers. If you click on the gallery or camera uploader pages, it will either
ask for an image from your files, or it will ask you to take a picture. Before allowing either of these, the program will request permissions for both the
gallery and camera. After choosing the image you wish to be recognized, the machine learning model will attempt to recognize both the animal and breed of the
image. After that, you can click a button called description to see a description page of the animal. 

The description page shows their name, breed, and description and takes you back home once the home button is clicked. The database is in the assets older;
however, the SQLite functions (create table and queries) are not being reflected in the results (still produces the same desired results). ****
