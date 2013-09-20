package edu.nyu.cs.cs2580;

import java.util.regex.Pattern;



/**
  * Stemmer, implementing the Porter Stemming Algorithm
  *
  * The Stemmer class transforms a word into its root form.  The input
  * word can be provided a character at time (by calling add()), or at once
  * by calling one of the various stem(something) methods.
  */

public class Stemmer {  
	
	private char[] b;
	private int i,     /* offset into b */
               	i_end, /* offset to end of stemmed word */
               	j, k;
	private static final int INC = 50;
                     /* unit of size whereby b is increased */
	public Stemmer() {  
		b = new char[INC];
		i = 0;
		i_end = 0;
	}

	/**
	 * Add a character to the word being stemmed.  When you are finished
	 * adding characters, you can call stem(void) to stem the word.
	 */
	public void add(char ch) {  
		if (i == b.length) {  
			char[] new_b = new char[i+INC];
			for (int c = 0; c < i; c++) new_b[c] = b[c];
			b = new_b;
		}
		b[i++] = ch;
	}


	/** Adds wLen characters to the word being stemmed contained in a portion
	 * of a char[] array. This is like repeated calls of add(char ch), but
	 * faster.
	 */
	public void add(char[] w, int wLen) {  
		if (i+wLen >= b.length) {  
			char[] new_b = new char[i+wLen+INC];
			for (int c = 0; c < i; c++) new_b[c] = b[c];
			b = new_b;
		}
		for (int c = 0; c < wLen; c++) {
			b[i++] = w[c];
		}
	}

	/**
	 * After a word has been stemmed, it can be retrieved by toString(),
	 * or a reference to the internal buffer can be retrieved by getResultBuffer
	 * and getResultLength (which is generally more efficient.)
	 */
	public String toString() { 
		return new String(b,0,i_end); 
	}

	/**
	 * Returns the length of the word resulting from the stemming process.
	 */
	public int getResultLength() { 
		return i_end; 
	}

	/**
	 * Returns a reference to a character buffer containing the results of
	 * the stemming process.  You also need to consult getResultLength()
	 * to determine the length of the result.
	 */
	public char[] getResultBuffer() { 
		return b; 
	}

	/* cons(i) is true <=> b[i] is a consonant. */

	private final boolean cons(int i) {  
		switch (b[i]) {  
			case 'a': case 'e': case 'i': case 'o': case 'u': return false;
			case 'y': return (i==0) ? true : !cons(i-1);
			default: return true;
		}
	}

   /* m() measures the number of consonant sequences between 0 and j. if c is
      a consonant sequence and v a vowel sequence, and <..> indicates arbitrary
      presence,
         <c><v>       gives 0
         <c>vc<v>     gives 1
         <c>vcvc<v>   gives 2
         <c>vcvcvc<v> gives 3
         ....
   */

	private final int m() {  
		int n = 0;
		int i = 0;
		while(true) {  
			if (i > j) {
				return n;
			}
			if (! cons(i)) {
				break; 
			}
			i++;
      }
      i++;
      while(true) {  
    	  while(true) {  
    		  if (i > j) {
    			  return n;
    		  }
               if (cons(i)) {
            	   break;
               }
               i++;
         }
         i++;
         n++;
         while(true) {  
        	 if (i > j) {
        		 return n;
        	 }
            if (! cons(i)) {
            	break;
            }
            i++;
         }
         i++;
       }
   }

   /* vowelinstem() is true <=> 0,...j contains a vowel */
   private final boolean vowelinstem() {  
	   int i; for (i = 0; i <= j; i++) if (! cons(i)) return true;
      return false;
   }

   /* doublec(j) is true <=> j,(j-1) contain a double consonant. */
   private final boolean doublec(int j) {  
	   if (j < 1) {
		   return false;
	   }
	   if (b[j] != b[j-1]) {
		   return false;
	   }
      return cons(j);
   	}

   /* cvc(i) is true <=> i-2,i-1,i has the form consonant - vowel - consonant
      and also if the second c is not w,x or y. this is used when trying to
      restore an e at the end of a short word. e.g.

         cav(e), lov(e), hop(e), crim(e), but
         snow, box, tray.

   */
	private final boolean cvc(int i) {  
		if (i < 2 || !cons(i) || cons(i-1) || !cons(i-2)) return false;
      {  int ch = b[i];
         if (ch == 'w' || ch == 'x' || ch == 'y') return false;
      }
      return true;
   }

	private final boolean ends(String s) {  
		int l = s.length();
		int o = k-l+1;
		if (o < 0) {
			return false;
		}
		for (int i = 0; i < l; i++) {
			if (b[o+i] != s.charAt(i)) {
				return false;
			}
		}
		j = k-l;
		return true;
   }

	/* setto(s) sets (j+1),...k to the characters in the string s, readjusting
      k. */
	private final void setto(String s) {  
		int l = s.length();
		int o = j+1;
		for (int i = 0; i < l; i++) {
			b[o+i] = s.charAt(i);
		}
		k = j+l;
	}

	/* r(s) is used further down. */
	private final void r(String s) { 
		if (m() > 0) setto(s); 
	}

	/* step1ab() gets rid of plurals and -ed or -ing. e.g.
          caresses  ->  caress
          ponies    ->  poni
          ties      ->  ti
          caress    ->  caress
          cats      ->  cat
          feed      ->  feed
          agreed    ->  agree
          disabled  ->  disable
          matting   ->  mat
          mating    ->  mate
          meeting   ->  meet
          milling   ->  mill
          messing   ->  mess
          meetings  ->  meet
	 */
	private final void step1ab() {  
		if (b[k] == 's') {  
			if (ends("sses")) {
				k -= 2; 
			}
			else if (ends("ies")) {
				setto("i"); 
			}
			else if (b[k-1] != 's') {
				k--;
			}
		}
		if (ends("eed")) { if (m() > 0) k--; } else
			if ((ends("ed") || ends("ing")) && vowelinstem())
			{  k = j;
			if (ends("at")) setto("ate"); else
				if (ends("bl")) setto("ble"); else
					if (ends("iz")) setto("ize"); else
						if (doublec(k)) {  
							k--;
							{  int ch = b[k];
								if (ch == 'l' || ch == 's' || ch == 'z') k++;
							}
						}
						else if (m() == 1 && cvc(k)) 
							setto("e");
			}
	}

	/* step1c() turns terminal y to i when there is another vowel in the stem. */
	private final void step1c() { 
		if (ends("y") && vowelinstem()) {
			b[k] = 'i'; 
		}
	}

	/** Stem the word placed into the Stemmer buffer through calls to add().
	 * Returns true if the stemming process resulted in a word different
	 * from the input.  You can retrieve the result with
	 * getResultLength()/getResultBuffer() or toString().
	 */
	public void innerStem() {  
		k = i - 1;
		if (k > 1) { 
			step1ab(); 
			step1c(); 
		}
		i_end = k+1; i = 0;
	}

	/** method used for stem a word
	 * return stemmed root word
	 */
	public String stem(String st) {
		Pattern p = Pattern.compile("[^a-zA-Z0-9]");
		boolean hasSpecialChar = p.matcher(st).find();
		if(!hasSpecialChar) {
			char[] w = new char[501];
		    char[] charArray = st.toCharArray();
		    for (int i = 0; i < charArray.length; i++) {
		    	w[i] = charArray[i];
	 			add(w[i]); 			
		    }
		    innerStem();
		    return toString();
		}
		return st;
	}
	
//	public static void main(String[] args) {
//		Stemmer s = new Stemmer();
//		String st = "computers";
//		System.out.println(s.stem(st));
//	}
}