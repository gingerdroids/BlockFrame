package org.blockframe.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.blockframe.core.Block.PlacedBlock;


/**
 * Logs debugging messages. 
 * <p>
 * The fields {@link Block#loggingVerbosity}, {@link Canvas#loggingVerbosity} and {@link #loggingVerbosity} are used to filter messages. 
 * Conventional values for verbosity can be found in {@link Verbosity}. 
 * This interface can be inherited by classes (eg, {@link Block}), injecting the constants into the class's name-space. 
 * <p>
 * Calls to the {@link #add(double, Block, Canvas, String, boolean)} (or similar) method with verbosity less than {@link Verbosity#WARNING_4} 
 * should be written specifically for the current bug, and be deleted when it is solved. 
 * It would be surprising to have a call with such a low level in production code. 
 * <p>
 * Nothing is written to the console until {@link #out()} or {@link #err()} are called. 
 * This can be annoying when you've got an infinite loop, but it does make the {@link #makeCheckpoint()} functionality possible. 
 */
public class DebugLog { 
	
	/**
	 * Constants giving conventional debug-verbosity levels. 
	 * <p>
	 * Each {@link Block} and {@link Canvas} has a messaging level. Also, {@link DebugLog} has a global level. 
	 * Likewise, each message has a required verbosity level. 
	 * If the current level of the block (or of the canvas, or the global level) is greater than the message's required level, the message is recorded. 
	 * (Otherwise, it is discarded.)
	 * <p>
	 * So, setting the {@link Block#loggingVerbosity} high will log musch detail, setting it to zero means nothing will be logged. 
	 * <p>
	 * A message with a low required verbosity will usually be recorded. 
	 * Messages with a high required verbosity will recorded in fewer blocks. 
	 * <p>
	 * Messages with a verbosity under {@link #WARNING_4} should only be temporary, while debugging code. 
	 * The permanent code should not have any messages with very low verbosity. 
	 */
	public static interface Verbosity {
		
		/** No messages. */
		public static final double NONE_0 = 0 ; 
		
		/** Temporary messages, while chasing a particular bug. */
		public static final double BUG_CONTEXT_2 = 2 ; 
		
		/** Temporary messages, while chasing a particular bug. */
		public static final double BUG_DETAIL_3 = 3 ; 
		
		/** Messages warning about unexpected conditions. For example, a child's measured area going outside its container's measured area. */
		public static final double WARNING_4 = 4 ; 

		/** Records entering the major methods of the block, such as {@link Block#fill(Quill, Layout)} and {@link Block#draw(Canvas, double, double)}. */
		public static final double ENTERING_5 = 5 ; 
		
		/** Records entering and leaving the major methods of the block, such as {@link Block#fill(Quill, Layout)} and {@link Block#draw(Canvas, double, double)}. */
		public static final double LEAVING_6 = 6 ; 
		
		/** Messages about broad state of the block's operations. */
		public static final double GENERAL_7 = 7 ; 
		
		/** Messages about detail of the block's operations. */
		public static final double DETAIL_8 = 8 ; 
		
		/** All messages. */
		public static final double ALL_9 = 9 ; 
	}
	
	/**
	 * List of unprinted messages. 
	 */
	public static final ArrayList<String> messages = new ArrayList<String>(); 
	
	private static final Object lock = new Object(); 
	
	/**
	 * Global verbosity threshold. 
	 * In this class, we should almost always leave the value at zero. 
	 */
	public static double loggingVerbosity = 0 ; 
	
	/**
	 * The thread that made the last call to <code>add</code> a message. 
	 */
	private static Thread prevThread ;

	/**
	 * Clears the list of messages (@link {@link #messages}). 
	 */
	public static void clear() { 
		synchronized (lock) {
			messages.clear(); 
		}
	}
	
	/**
	 * Writes all messages to <code>System.out</code>., then clears the list 
	 */
	public static void out() { 
		synchronized (lock) {
			for (String message : messages) System.out.println(message); 
			messages.clear(); 
		}
	}
	
	/**
	 * Writes all messages to <code>System.err</code>, then clears the list. 
	 */
	public static void err() { 
		synchronized (lock) {
			for (String message : messages) System.err.println(message); 
			messages.clear(); 
		}
	}
	
	/**
	 * Writes all messages to the given file, then clears the list. 
	 */
	public static void file(File file) throws IOException { 
		synchronized (lock) {
			PrintWriter writer = new PrintWriter(file); 
			for (String message : messages) writer.println(message); 
			writer.close(); 
			messages.clear(); 
		}
	}
	
	/**
	 * Notes any changes to which thread is logging messages. 
	 * This should be called inside the <code>synchronized</code> code, prior to adding to {@link #messages}. 
	 */
	private static void noteThread() { 
		Thread thread = Thread.currentThread(); 
		if (thread!=prevThread) { 
			messages.add("---------------- Thread is:  "+thread.getName()); 
			prevThread = thread ; 
		}
	}
	
	// TODO Code the missing addPlain() methods. 
	
	/**
	 * Similar to {@link #add(double, Block, Canvas, String, boolean)}, except that the message is written as is, with no prefix or stack. 
	 */
	public static void addPlain(double requiredVerbosity, Block block, Canvas canvas, String message) { 
		if (requiredVerbosity<=0) return ; 
		if ((block!=null&&requiredVerbosity<=block.loggingVerbosity) || (canvas!=null&&requiredVerbosity<=canvas.loggingVerbosity) || (requiredVerbosity<=loggingVerbosity)) { 
			synchronized (lock) {
				noteThread(); 
				messages.add(message); 
			}
		}
	}

	/**
	 * Similar to {@link #add(double, Block, Canvas, String, boolean)}, except that the message is written as returned from the {@linkplain StringGetter}, with no prefix or stack. 
	 */
	public static void addPlain(double requiredVerbosity, PlacedBlock placedBlock, Canvas canvas, StringGetter messageGetter, Object messageArg0, Object messageArg1) { 
		if (requiredVerbosity<=0) return ; 
		Block block = placedBlock.getBlock();
		if ((block!=null&&requiredVerbosity<=block.loggingVerbosity) || (canvas!=null&&requiredVerbosity<=canvas.loggingVerbosity) || (requiredVerbosity<=loggingVerbosity)) { 
			synchronized (lock) {
				noteThread(); 
				messages.add(messageGetter.getString(block, placedBlock, messageArg0, messageArg1)); 
			}
		}
	}

	/**
	 * Always adds the message to the debug-log. 
	 */
	public static void add(Block block, String message, boolean wantStack) {
		synchronized (lock) {
			noteThread(); 
			String fullMessage = "" ; 
			if (block!=null) fullMessage += block.getLogName()+"  " ; 
			if (message!=null) fullMessage += message+"  " ; 
			if (wantStack) fullMessage += "\t"+currentStack();
			messages.add(fullMessage); 
		}
	}

	/**
	 * Always adds the message to the debug-log. 
	 */
	private static void add(Block block, PlacedBlock placedBlock, StringGetter messageGetter, Object messageArg0, Object messageArg1, boolean wantStack) {
		add(block, messageGetter.getString(block, placedBlock, messageArg0, messageArg1), wantStack);
	}

	/**
	 * Always adds the message to the debug-log. 
	 */
	public static void add(Block block, StringGetter messageGetter, Object messageArg0, Object messageArg1, boolean wantStack) {
		add(block, null, messageGetter, messageArg0, messageArg1, wantStack);
	}

	/**
	 * Always adds the message to the debug-log. 
	 */
	public static void add(PlacedBlock placedBlock, StringGetter messageGetter, Object messageArg0, Object messageArg1, boolean wantStack) {
		add(placedBlock.getBlock(), placedBlock, messageGetter, messageArg0, messageArg1, wantStack);
	}
	
	// TODO Code the missing add()methods. 
	
	/**
	 * Adds the message if its <code>requiredVerbosity</code> is equal or less any of the verbosities in {@link Block}, {@link Canvas} or globally. 
	 * However, if the <code>level</code> is zero or less, the message is discarded. 
	 * <p>
	 * The message is prefixed with the block's name, and optionally followed by a compact listing of the calling stack. 
	 * @see Block#getLogName()
	 */
	public static void add(double requiredVerbosity, Block block, Canvas canvas, String message, boolean wantStack) { 
		if (requiredVerbosity<=0) return ; 
		if ((block!=null&&requiredVerbosity<=block.loggingVerbosity) || (canvas!=null&&requiredVerbosity<=canvas.loggingVerbosity)  || (requiredVerbosity<=loggingVerbosity)) { 
			add(block, message, wantStack);
		}
	}

	/**
	 * Adds the message if its <code>requiredVerbosity</code> is equal or less any of the verbosities in {@link Block}, {@link Canvas} or globally. 
	 * However, if the <code>level</code> is zero or less, the message is discarded. 
	 * <p>
	 * The message is prefixed with the block's name, and optionally followed by a compact listing of the calling stack. 
	 * @see Block#getLogName()
	 */
	public static void add(double requiredVerbosity, Block block, Canvas canvas, StringGetter messageGetter, Object messageArg0, Object messageArg1, boolean wantStack) { 
		if (requiredVerbosity<=0) return ; 
		if ((block!=null&&requiredVerbosity<=block.loggingVerbosity) || (canvas!=null&&requiredVerbosity<=canvas.loggingVerbosity)  || (requiredVerbosity<=loggingVerbosity)) { 
			add(block, null, messageGetter, messageArg0, messageArg1, wantStack);
		}
	}

	/**
	 * Adds the message if its <code>requiredVerbosity</code> is equal or less any of the verbosities in {@link Block}, {@link Canvas} or globally. 
	 * However, if the <code>level</code> is zero or less, the message is discarded. 
	 * <p>
	 * The message is prefixed with the block's name, and optionally followed by a compact listing of the calling stack. 
	 * 
	 * @param placedBlock The {@linkplain Block} associated with this is used. 
	 * @see Block#getLogName()
	 */
	public static void add(double requiredVerbosity, PlacedBlock placedBlock, Canvas canvas, StringGetter messageGetter, Object messageArg0, Object messageArg1, boolean wantStack) { 
		if (requiredVerbosity<=0) return ; 
		Block block = placedBlock.getBlock(); 
		if ((block!=null&&requiredVerbosity<=block.loggingVerbosity) || (canvas!=null&&requiredVerbosity<=canvas.loggingVerbosity)  || (requiredVerbosity<=loggingVerbosity)) { 
			add(placedBlock, messageGetter, messageArg0, messageArg1, wantStack);
		}
	}
	
	/**
	 * Makes a checkpoint after the most recent message. 
	 * If {@link MessagesCheckpoint#revert()} is called, all messages between now and then are discarded. 
	 */
	public static MessagesCheckpoint makeCheckpoint() { 
		return new MessagesCheckpoint(); 
	}
	
	/**
	 * Holds a checkpoint. 
	 * @see DebugLog#makeCheckpoint()
	 */
	public static class MessagesCheckpoint { 
		
		private final int count;

		private MessagesCheckpoint() { 
			this.count = messages.size(); 
		}
		
		public void revert() { 
			while (messages.size()>count) messages.remove(messages.size()-1); 
		}
	}

	// TODO StringGetter implementations should handle null messageArgs sensibly. 
	
	public static interface StringGetter { 
		String getString(Block block, PlacedBlock placedBlock, Object messageArg0, Object messageArg1); 
	}

	public static final StringGetter stack = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object ignore0, Object ignore1) { 
			return currentStack();
		}
	};
	
	/**
	 * Returns a single-line form of the current stack. 
	 */
	public static String currentStack() { 
		return theStack(Thread.currentThread().getStackTrace(), 0); 
	}
	
	private static String theStack(StackTraceElement[] stackTrace, int skipCount) { 
		StringBuilder sb = new StringBuilder(); 
		build(sb, stackTrace, skipCount);
		return sb.toString();
	}
	
	/**
	 * Appends the stack-trace to the string-builder. 
	 * A compact single-line version of the stack is appended - most classes are omitted, including android library classes. 
	 * <p>
	 * @param skipCount The number of items on the stack to be skipped - mostly calls within this class. 
	 */
	private static void build(StringBuilder sb, StackTraceElement[] stackTrace, int skipCount) {
		String prevClassFullName = null ; // Prev-iteration value of 'classFullName' 
		boolean wasInAppCode = true ; // Prev-iteration value of 'isInAppCode' 
		for (int i=skipCount ; i<stackTrace.length ; i++) {
			StackTraceElement stackItem = stackTrace[i];
			String classFullName = stackItem.getClassName();  
			if (classFullName.startsWith(DebugLog.class.getName())) continue ; 
			boolean isInAppCode = true ; 
			if (classFullName.startsWith("java.")) isInAppCode = false ; 
			else if (classFullName.startsWith("javax.")) isInAppCode = false ; 
			else if (classFullName.startsWith("com.android.")) isInAppCode = false ; 
			else if (classFullName.startsWith("android.")) isInAppCode = false ; 
			else if (classFullName.startsWith("dalvik.")) isInAppCode = false ; 
			if (isInAppCode) { 
				if (classFullName.equals(prevClassFullName)) { 
					sb.append(","); 
				} else { 
					if (prevClassFullName!=null) sb.append(" "); 
					String[] parts = classFullName.split("\\."); // 'split()' takes a regular-expression argument. 
					try { 
						sb.append(parts[parts.length-1]); 
					} catch (Exception e) {
						sb.append("*"+classFullName); 
					}
					sb.append(":"); 
				}
				sb.append(stackItem.getLineNumber()); 
			} else { 
				if (wasInAppCode) sb.append("*"); 
			}
			prevClassFullName = classFullName ; 
			wasInAppCode = isInAppCode ; 
		}
	}

}
