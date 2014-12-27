package IC.Types;

/**
 * This is a type entry for the compilation process type table.
 * 
 * @author Asaf Bruner, Aviv Goll
 */
public abstract class Type {
	private String name;
	private int ID;
	
	private static int NextID = 0;
	
	public Type(String name) {
		super();
		this.name = name;
		this.ID = NextID++;
	}

	/**
	 * @param t another type
	 * @return true if t is a parent of the type or the same type
	 */
	public boolean subtypeof(Type t) {
		return (this == t);
	}

	/**
	 * 
	 * @return the type name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for the name
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @return the type ID
	 */
	public int getID() {
		return ID;
	}

	@Override
	public String toString() {
		return name;
	}

}
