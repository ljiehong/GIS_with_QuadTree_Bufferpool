import cs3114.GIS.DS.Hashable;

import java.util.ArrayList;

public class nameEntry implements Hashable<nameEntry> {
    private String key; // GIS feature name
    private ArrayList<Long> locations; // file offsets of matching records

    /**
     * Initialize a new nameEntry object with the given feature name
     * and a single file offset.
     * @param name feature name
     * @param offset file offset.
     */
    public nameEntry(String name, Long offset) {
        key=name;
        locations=new ArrayList<>();
        locations.add(offset);
    }

    /**
     * Return feature name.
     * @return feature name
     */
    public String key() {
        return key;
    }

    /**
     * Return list of file offsets.
     * @return list of file offsets
     */
    public ArrayList<Long> locations() {
        return locations;
    }

    /**
     * Append a file offset to the existing list.
     * @param offset file offset
     * @return Adding success returns true
     */
    public boolean addLocation(Long offset) {
        if(!locations.contains(offset)){
            return locations.add(offset);
        }
        return false;
    }

    /**
     * Donald Knuth hash function for strings. You MUST use this.
     * @return hash value
     */
    public int Hash() {
        int hashValue = key.length();
        for (int i = 0; i < key.length(); i++) {
            hashValue = ((hashValue << 5) ^ (hashValue >> 27)) ^ key.charAt(i);
        }
        return (hashValue & 0x0FFFFFFF);
    }

    /**
     * Two nameEntry objects are considered equal iff they
     * hold the same feature name.
     * @return Returns true if key is the same
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        nameEntry nameEntry = (nameEntry) o;

        return key != null ? key.equals(nameEntry.key) : nameEntry.key == null;
    }

    /**
     * Return a String representation of the nameEntry object in the
     * format needed for this assignment.
     * @return [key,locations]
     */
    public String toString() {
        return ("[" + this.key + ", " + this.locations.toString() + "]");
    }
}