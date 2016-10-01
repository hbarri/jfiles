/*
 * Copyright (C) 2016 - WSU CEG3120 Students
 * 
 * Roberto C. Sánchez <roberto.sanchez@wright.edu>
 * John T. Wintersohle II <Dorkatron199@aols.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package edu.wright.cs.jfiles.fileapi;

/**
 * <p>
 * This is the class that provides functionality for managing JFile objects.
 * Examples of things this class is in charge of will be copying, deleting,
 * moving, renaming, and pasting JFiles.
 * </p>
 * 
 * <p>
 * It is the goal of this class to work as a medium of communication between
 * <b>security</b> (such as authentication, authorization, and accounting),
 * <b>maintenance</b> (such as logging), <b>the GUI</b> (such as searching), and
 * <b>JFiles</b>, by incorporating the methods of all of these other APIs into
 * the File API of JFiles.
 * </p>
 * 
 * <p>
 * This also exists to separate the methods involved with manipulating JFiles
 * from an external point of view from JFiles themselves. This is done so that a
 * GUI or CLI can better make use of the File API.
 * </p>
 * 
 * @author <b>Team 5:</b>
 * @author John Wintersohle II
 *         <<a href="mailto:Dorkatron199@aol.com">Dorkatron199@aol.com</a>>
 *
 */
public class JFileManager {

	/**
	 * This is the clipboard used when copying JFiles. It is capable of copying
	 * and pasting multiple JFiles at a time. It is made private so that it can
	 * only be accessed via the JFileManager methods for the sake of security.
	 * 
	 */
	// This is suppressed until we build in the functionality for this field.
	// This will most likely be when we make the paste() method.
	@SuppressWarnings("unused")
	private JFile[] clipboard;

	/**
	 * <p>
	 * Cuts the file passed in.
	 * </p>
	 * 
	 * <p>
	 * This method calls copy then delete on the file being passed in. Adds the
	 * file to the top of the paste stack then moves the file to the trash
	 * directory set up during configuration.
	 * </p>
	 * 
	 * @param files
	 *            The files being cut.
	 * 
	 */
	public void cut(JFile[] files) {
		copy(files);
		delete(files);
	}

	/*
	 * This needs to be made into a deep copy so that the contents of the
	 * clipboard can still be accessed even of the user later deletes the files.
	 * I will work on this later.
	 * 
	 * Though the clone method is used, I am not happy until its exact
	 * functionality is defined in JFile.
	 */

	/**
	 * This method copies all of the files passed in to the clipboard.
	 * 
	 * @param files
	 *            The files being copied to the clipboard.
	 * 
	 */
	public void copy(JFile[] files) {
		clipboard = files.clone();
	}

	/**
	 * This method will copy the contents of the clipboard into the directory at
	 * a specified location name.
	 * 
	 * @param dirName
	 *            The name of the directory being pasted to.
	 * 
	 */
	public void paste(String dirName) {

	}

	/**
	 * Deletes a particular JFile object. If the file(s) is/are not in the trash
	 * bin, it moves it/them to that location. If they are, then it deletes them
	 * off of the hard drive the same way the OS would.
	 * 
	 * @param files
	 *            The files being deleted.
	 * 
	 */
	public void delete(JFile[] files) {

	}

	/*
	 * We need to determine how this will be done.
	 *
	 * Can it be done in one action, or does it need to copy the contents, then
	 * paste them, then delete? As one can see, this will put it all together,
	 * meaning that moving a file will test all of this functionality the other
	 * route.
	 *
	 * My primary concern with the later option is that it would mess with the
	 * clipboard, even though the user didn't do a copy-like action that would
	 * make sense for copying to the clip board. The danger in this is that the
	 * user may lose what they currently have on the clipboard as a result,
	 * since they would not be expecting it to override what is currently in it.
	 * 
	 * One way to get around this is to use a temporary variable in the method
	 * and do all of the swapping around internally. It will make the method a
	 * bit longer, but may make the functionality closer to what we want.
	 * 
	 * I suggest we discuss this at some point.
	 */

	/**
	 * This method moves a file or list of files from one location to another.
	 * 
	 * @param files
	 *            The files being moved.
	 * @param dirName
	 *            The name of the directory where files will be moved to.
	 */
	public void move(JFile[] files, String dirName) {

	}

	/**
	 * Renames a file.
	 * 
	 * @param oldName
	 *            The location of the file being renamed.
	 * @param newName
	 *            The name that the file is changing to.
	 * 
	 */
	public void rename(String oldName, String newName) {

	}

	/**
	 * Opens the file with the name passed in using the default application
	 * associated with this kind of file. How this is determined will be
	 * different for each operating system.
	 * 
	 * @param file
	 *            The file being opened.
	 * 
	 */
	public void open(String file) {

	}

	/**
	 * Opens the file passed in with a particular application which is also
	 * passed in.
	 * 
	 * @param name
	 *            The name of the file being opened.
	 * 
	 */

	public void openWith(String name/* , Application app */) {

	}

	/*
	 * We need to determine what properties we need to add to details. This data
	 * may be used by other teams, and when we determine what properties we are
	 * returning, we will need to update it in our documentation.
	 */

	/**
	 * Returns various details of a file in key-value pairs. By this, the user
	 * can ask for a specific key, such as name, size, or type, and get an
	 * appropriate response.
	 * 
	 * @param name
	 *            The name of the file being displayed.
	 * 
	 */
	public void getDetails(String name) {

	}
	
	
	// We may not use this or put this method's functionality in the getDetails
	// method.
	/**
	 * Returns the type of the file passed in. This method's functionality will
	 * change depending on which OS the user is using.
	 * 
	 */
	public void getType() {

	}
}