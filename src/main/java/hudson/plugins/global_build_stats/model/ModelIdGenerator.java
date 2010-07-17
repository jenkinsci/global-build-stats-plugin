package hudson.plugins.global_build_stats.model;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ModelIdGenerator {
	
	public static final ModelIdGenerator INSTANCE = new ModelIdGenerator();
	
	private static final int KEY_DEPTH = 32;
	private static final char[] AVAILABLE_CHARS = new char[]{
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '@', '$', '*', '%', '!', '#'
	};
	
	private Map<Class, Set<String>> registeredIds = Collections.synchronizedMap(new HashMap<Class, Set<String>>());
	
	private ModelIdGenerator(){
	}
	
	public void registerIdForClass(Class clazz, String id){
		if(!registeredIds.containsKey(clazz)){
			// Set should be synchronized
			registeredIds.put(clazz, Collections.synchronizedSet(new HashSet<String>()));
		}
		registeredIds.get(clazz).add(id);
	}
	
	public String generateIdForClass(Class clazz){
		String id = null;
		do{
			StringBuilder b = new StringBuilder();
			Random r = new SecureRandom();
			for(int i=0; i<KEY_DEPTH; i++){
				b.append(AVAILABLE_CHARS[r.nextInt(AVAILABLE_CHARS.length)]);
			}
			id = b.toString();
		// Re-generating id if it already exists
		}while(registeredIds.containsKey(clazz) && registeredIds.get(clazz).contains(id));		
		
		registerIdForClass(clazz, id);
		
		return id;
	}
}
