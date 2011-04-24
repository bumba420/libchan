clichan requires Java, you can download it at [1].

You run the jar file by calling
	java -jar cliChan.jar

A large variety of imageboards is supported, to get a list of Imageboards supported by cliChan, call
	java -jar cliChan.jar -list

You can easiely write a template for your favourite imageboard, should it not be supported, and place it in the chans/ folder. (TODO: write documentation)

To download all images from a thread, simply call
	java -jar cliChan.jar -u http://boards.4chan.org/b/res/123456

The images will be saved to the a folder for the thread with subfolders for each user, this folder is either located in the current directory or in the location specified 
by the -o option. Set the -nothreadfolders flag to avoid having a subfolder for each thread.
Adding the -html flag will save a html representation of the thread, thumbnails are stored in the .thumbs/ folder for each thread.

Use the -i parameter to set a refresh interval, so that the thread is monitored and new images are saved automatically. Clichan will also attempt to detect follow-up 
threads, therefore it will also follow all threads mentioned after the 'new thread' tag (case insensitive, can be changed using the -tag option)
E.g. call
	java -jar cliChan.jar -i 120 -html -u http://boards.4chan.org/b/res/123456

to refresh the thread every 120s to save images and html.

The -noarchive option will disable saving of images, thumbnails however are still saved when used in combindation with the -html option.
Adding -stats to the option list will result in the creation of a .csv file (fields are tab seperated), containing statistics for each poster in the thread, and .gv file 
that visualizes the relation between the posters.
E.g. call
	java -jar cliChan.jar -noarchive -nothreadfolders -stats -u http://boards.4chan.org/b/res/123456

to get just the statistical data to the current directiry without saving anything else, now call e.g.
	dot -Tsvg 123456.gv -o 123456.svg

for a graphical representation of the relations between users.


[1] http://www.java.com/download/
