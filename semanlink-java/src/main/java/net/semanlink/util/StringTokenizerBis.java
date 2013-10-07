package net.semanlink.util;
import java.util.*;

/** StringTokenizer ne retourne pas les items vides, StringTokenizerBis si.

rq : vite fait

@author hyperFP
@version 0.02 (juin 99, Tasmaniac) ; 0.01 (3 fev 99)
*/

public class StringTokenizerBis {
char[] c;
int theLength;
int ouCommenceLeNextToken = 0;
char delim;

public StringTokenizerBis(String s,char delim) {
	this.delim = delim;
	c = s.toCharArray();
	theLength = c.length;
}

public boolean hasMoreTokens() {
	return (ouCommenceLeNextToken < theLength);
}

public String nextToken() {
	int debutDuToken = ouCommenceLeNextToken;
	int countInToken = 0;

	// ligne Tasmaniac
	if (ouCommenceLeNextToken >= theLength) throw new NoSuchElementException ();

	for (; ouCommenceLeNextToken < theLength;ouCommenceLeNextToken++) {
		if (c[ouCommenceLeNextToken] == delim) {
			// on a atteint la fin du token. Il faut passer ce car pour la prochaine fois :
			ouCommenceLeNextToken++;
			break;
		} else {
			// le caractère est dans le token
			countInToken++;
		}
	}
	return new String(c,debutDuToken,countInToken);
}
      
                

//
// following lines brought to you by courtesy of Tasmanic
//

/** Renvoie le nombre d'items */
public int numberOfTokens () {
	return countOfTokens (0);
}

/** Renvoie le nombre d'items restant en fonction de l'avancement. */
public int countTokens () {
	return countOfTokens (ouCommenceLeNextToken);
}

/** Renvoie le nombre d'items restant à partir d'une position.*/
private int countOfTokens (int ouCommencerACompter) {
	int nbTokens = 1;
	boolean auMoinsUnToken = false;
	while (ouCommencerACompter < theLength) {		
			if (c[ouCommencerACompter] == delim) {
				nbTokens ++;
				auMoinsUnToken = true;
			}
			ouCommencerACompter++;
	}
	if (!auMoinsUnToken) nbTokens--;
	return (nbTokens);
}

}

