import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Indexing 
{
	private static BufferedReader br;
	private static BufferedReader br1;
	
	private static TreeMap<String, Integer> listOfWords = new TreeMap<String, Integer>();
	private static Set<String> tag_names = new HashSet<String>();
	private static ArrayList<File> files = new ArrayList<File>();
	private static TreeMap<String, ArrayList<String>> doclist = new TreeMap<String, ArrayList<String>>();
	private static TreeMap<String, Integer> doc_count = new TreeMap<String, Integer>();
	private static TreeMap<String, TreeMap<String, Integer>> term_count = new TreeMap<String, TreeMap<String, Integer>>();
	private static TreeMap<String, TreeMap<String, Integer>> word_occurances = new TreeMap<String, TreeMap<String, Integer>>();
	private static TreeMap<String, Map.Entry<String, Integer>> most_frequent = new TreeMap<String, Map.Entry<String, Integer>>();
	private static TreeMap<String, Integer> wordsindoc = new TreeMap<String, Integer>();
	private static TreeMap<String, Values> dictionary = new TreeMap<String, Values>();
	private static TreeMap<String, ArrayList<PostingsValues>> postings = new TreeMap<String, ArrayList<PostingsValues>>();
	private static TreeMap<String, ArrayList<PostingsValues>> compressed_postings = new TreeMap<String, ArrayList<PostingsValues>>();
	private static TreeMap<String, HashMap<Values, ArrayList<PostingsValues>>> index = new TreeMap<String, HashMap<Values,ArrayList<PostingsValues>>>();
	private static TreeMap<String, HashMap<Values, ArrayList<PostingsValues>>> compressed_index = new TreeMap<String, HashMap<Values,ArrayList<PostingsValues>>>();
	
	public static void main(String args[]) throws IOException, ClassCastException
	{
		float start_time = System.nanoTime(); // The starting time of the program.
		
		//Takes the folder location as an argument from the user.
		final File folder = new File(args[0]);
		
		//Takes location of stopwords from the user.
		final File stopwords = new File(args[1]);
		
		//Takes location of WordList from the user.
		final File terms = new File(args[2]);
		
		//Create Results folder.
		final File results = new File("Results");
		results.mkdir();
		
		//Creates FinalIndices folder.
		final File FinalIndices = new File("FinalIndices");
		FinalIndices.mkdir();
		
		listFilesForFolder(folder);
		removeStopWords(listOfWords, stopwords);
		printListOfWords();
		
		/*System.out.println("Doclist showing which word is contained in which document");
		System.out.println(doclist);*/
		PrintWriter writer = new PrintWriter("./Results/DocumentList.txt", "UTF-8");
		writer.println(doclist);
		writer.close();
		//System.out.println("");
		
		/*System.out.println("Number of docs each term is contained in");*/
		numofDocs(doclist);
		/*System.out.println("");
		
		System.out.println("Number of terms in each document");*/
		numofTerms(folder, stopwords);
		writer = new PrintWriter("./Results/TermsInEachDocument.txt", "UTF-8");
		writer.println(term_count);
		writer.close();
		/*System.out.println(term_count);
		System.out.println("");*/
		
		//System.out.println("Most frequent stem in each document");
		writer = new PrintWriter("./Results/MostFrequentStem.txt", "UTF-8");
		writer.println(most_frequent);
		writer.close();
		//System.out.println(most_frequent);
		//System.out.println("");
		
		//System.out.println("Word occuraces in each document including stopwords");
		numofWordsIncStopWords(folder);
		writer = new PrintWriter("./Results/WordOccurrencesIncludingStopwords.txt", "UTF-8");
		writer.println(word_occurances);
		writer.close();
		/*System.out.println(word_occurances);
		System.out.println("");*/
		
		//System.out.println("Total words occurences in each document");
		writer = new PrintWriter("./Results/TotalWordOccurrences.txt", "UTF-8");
		writer.println(wordsindoc);
		writer.close();
		//System.out.println(wordsindoc);
		//System.out.println("");*/
		
		//System.out.println("The dictionary is as follows");
		float dic = Dictionary();
		printDictionary();
		//System.out.println("");
		
		//System.out.println("The postings is as follows");
		float post = Posting();
		printPostings();
		//System.out.println("");
		
		//System.out.println("Compressed postings");
		float comppost = compressedPostings();
		printCompressedPostings();
		//System.out.println("");
		
		float combine = createIndex();
		printIndex();
		
		float compcombine = createCompressedIndex();
		printCompressedIndex();
		
		float end_time = System.nanoTime(); // The ending time of the program.
		System.out.println("It took "+ (end_time - start_time)/1000000000  + " s to run the program."); 
		System.out.println("Check the directory for the dictionary, postings and compressed postings files. Other files are also included for ease of understanding. Take a look at the README and ProgramDescription files for more information");
		System.out.println("");
		
		float index_time = (dic + post + comppost + combine + compcombine)/1000000000;
		System.out.println("It took " + index_time + " to construct the index.");
		
		sizeOfUncompressedIndex();
		sizeOfCompressedIndex();
		NumberofInvertedLists();
		System.out.println("");
		
		Stemmer stem = new Stemmer();
		BufferedReader br = new BufferedReader(new FileReader(terms));
		String line;
		
		while((line = br.readLine()) != null)
		{
			int df = returnDf(stem.stripAffixes(line.toLowerCase()));
			int tf = returnTf(stem.stripAffixes(line.toLowerCase()));
			int listlen = returnInvertedListLength(stem.stripAffixes(line.toLowerCase()));
			
			System.out.println("Term: " + line + " \nDf: " + df + " \nTf: " + tf + " \nInverted List Length: " + listlen +" Bytes\n");
		}
		br.close();
	}
	
	public static void listFilesForFolder(final File folder) throws IOException 
	{
		int count = 0;
    	
		//For every file in the particular folder.
    	for (final File file : folder.listFiles()) 
	    {
    		//If the particular file in the folder is a folder in itself.
	        if (file.isDirectory()) 
	        {
	            listFilesForFolder(file); //Recursive function which finds the list of all the files in the directory.
	        } 
	        else 
	        {
	        	br = new BufferedReader(new FileReader(file)); //Initialize BufferedReader for that file.
	        	files.add(file);
	        }
	        Values val = new Values();
	        count = tagOperations(file, tag_names);
	        WordList(file, tag_names, val);
	    }
    	
	    //System.out.println("Number of tags found in the file: " + count); //Prints the number of tags in the file.
	    //printTagNames(tag_names); // Prints the tag names found in the file.
	}	
	
	/*
	 * This method takes the file as the argument and returns the number of tags in the particular file.
	 */
	public static int tagOperations(File file, Set<String> tag_names) throws IOException
	{
		String line;
		int tag_count = 0;
		
		br = new BufferedReader(new FileReader(file));
		
		while((line = br.readLine()) != null)
        {
			/*
			 * If the line contains a '<', it is considered a tag and tag_count is incremented.
			 */
        	if(line.contains("<"))
        	{
        		tag_count++;
        		
        		String b = line.replaceAll("[<>/]", "");
        		tag_names.add(b);
        	}
        	
        }
        tag_count/=2; //Since each tag represent the beginning and the end, we divide it by two to get the actual count.
        return tag_count;
	}
	
	/*
	 * This method prints the tag names found in the file.
	 */
	public static void printTagNames(Set<String>tag_names)
	{
		System.out.println("All the tags found in the file: " + tag_names);
	}
	
	/*
	 * Creates a hashtable with the tokens and their frequencies.
	 */
	public static void WordList(File file, Set<String> tag_names, Values val) throws IOException
	{
		String line;
		String words[];
		Stemmer stem = new Stemmer();
		
		br = new BufferedReader(new FileReader(file));
        br1 = new BufferedReader(new FileReader("stopwords"));
        
        while((line = br.readLine()) != null)
        {
        	String alphaOnly = line.replaceAll("[^a-zA-Z]+"," "); //Replace everything that is not an alphabet with a blank space.
      
        	words = alphaOnly.split(" ");
        	
            int countWord = 0;
            
            for(String word : words)
            {
            	if(!tag_names.contains(word) && !word.equals(""))
            	{
            		word = word.toLowerCase(); // Converts all words to lower case.
            		word = stem.stripAffixes(word);
            		//add word if it isn't added already
            		if(!listOfWords.containsKey(word))
            		{
            			if(!word.equals(""))
            				//first occurance of this word
            				listOfWords.put(word, 1);
            				listofDocs(word, file);
            				
            		}
            		else
            		{
            			countWord = listOfWords.get(word) + 1; //Get current count and increment
            			//listOfWords.remove(word); //First remove it (can't have duplicate keys)
            			listOfWords.put(word, countWord); //Now put it back with new value
            			listofDocs(word, file);
            			
            		}
            	}
            }
        }
        br1.close();
    }
	
	/*
	 * Finds the list of documents containing the term
	 */
	public static void listofDocs(String word, File file)
	{
		ArrayList <String> docs = new ArrayList<String>();
		
		if(!word.equals(""))
		{
			if(!doclist.containsKey(word))
			{
				docs.add(file.getName());
				doclist.put(word, docs);
			}
			else
			{
				docs = doclist.get(word);
				if(!docs.contains(file.getName()))
				{
					docs.add(file.getName());
					doclist.put(word, docs);
				}
			}
		}
	}
	
	/*
	 * Finds number of documents containing each term.
	 */
	public static void numofDocs(TreeMap<String, ArrayList<String>> doclist) throws FileNotFoundException, UnsupportedEncodingException
	{
		for(String key : doclist.keySet())
		{
			doc_count.put(key, doclist.get(key).size());
		}
		//System.out.println(doc_count);
		PrintWriter writer = new PrintWriter("./Results/DocumentCount.txt", "UTF-8");
		writer.println(doc_count);
		writer.close();
	}
	
	/*
	 * Finds number of times, terms occurs in each document.
	 */
	public static void numofTerms(File folder, File stopwords) throws IOException
	{
		String line;
		String words[];
		Stemmer stem = new Stemmer();
		
		for(File file : folder.listFiles())
		{
			TreeMap<String, Integer> tmap = new TreeMap<String, Integer>();
			Map.Entry<String, Integer> max = null;
			
			br = new BufferedReader(new FileReader(file));
			
			while((line = br.readLine()) != null)
	        {
	        	String alphaOnly = line.replaceAll("[^a-zA-Z]+"," "); //Replace everything that is not an alphabet with a blank space.
	      
	        	words = alphaOnly.split(" ");
	        	
	            int countWord = 0;
	            
	            for(String word : words)
	            {
	            	if(!tag_names.contains(word) && !word.equals(""))
	            	{
	            		word = word.toLowerCase(); // Converts all words to lower case.
	            		word = stem.stripAffixes(word);
	            		//add word if it isn't added already
	            		if(!tmap.containsKey(word))
	            		{
	            			if(!word.equals(""))
	            				//first occurance of this word
	            				tmap.put(word, 1);
	            		}
	            		else
	            		{
	            			countWord = tmap.get(word) + 1; //Get current count and increment
	            			tmap.put(word, countWord); //Now put it back with new value
	            		}
	            	}
	            }
	            word_occurances.put(file.getName(), tmap);
	            removeStopWords(tmap, stopwords);
	            term_count.put(file.getName(), tmap);
	            
	            for (Map.Entry<String, Integer> entry : tmap.entrySet())
	            {
	                if (max == null || entry.getValue().compareTo(max.getValue()) > 0)
	                {
	                    max = entry;
	                }
	            }
	            most_frequent.put(file.getName(), max);
	        }
		}
	}
	
	/*
	 * Number of word occurences including stopwords.
	 */
	public static void numofWordsIncStopWords(File folder) throws IOException
	{
		String line;
		String words[];
		Stemmer stem = new Stemmer();
		
		for(File file : folder.listFiles())
		{
			int count = 0;
			TreeMap<String, Integer> tmap = new TreeMap<String, Integer>();
			br = new BufferedReader(new FileReader(file));
			
			while((line = br.readLine()) != null)
	        {
	        	String alphaOnly = line.replaceAll("[^a-zA-Z]+"," "); //Replace everything that is not an alphabet with a blank space.
	      
	        	words = alphaOnly.split(" ");
	        	
	            int countWord = 0;
	            
	            for(String word : words)
	            {
	            	if(!tag_names.contains(word) && !word.equals(""))
	            	{
	            		word = word.toLowerCase(); // Converts all words to lower case.
	            		word = stem.stripAffixes(word);
	            		//add word if it isn't added already
	            		if(!tmap.containsKey(word))
	            		{
	            			if(!word.equals(""))
	            				//first occurrence of this word
	            				tmap.put(word, 1);
	            		}
	            		else
	            		{
	            			countWord = tmap.get(word) + 1; //Get current count and increment
	            			tmap.put(word, countWord); //Now put it back with new value
	            		}
	            	}
	            }
	            word_occurances.put(file.getName(), tmap);
	        }
			for(int val : tmap.values())
	        {
				count += val;
	        }
			wordsindoc.put(file.getName(), count);
		}
	}

	/*
	 * Prints the sorted hashtable with the words and their respective frequencies.
	 */
	public static void printListOfWords() throws FileNotFoundException, UnsupportedEncodingException
	{
		//System.out.println("The hashtable created with the <token : frequency> information:");
		//System.out.println(listOfWords + "\n");
		
		PrintWriter writer = new PrintWriter("./Results/Token-FrequencyInformation.txt", "UTF-8");
		writer.println("The hashtable created with the <token : frequency> information:");
		writer.println(listOfWords);
		writer.close();
	}
	
	/*
	 * Removes the stopwords from the index.
	 */
	public static void removeStopWords(TreeMap<String, Integer> listOfWords, File stopwords) throws IOException
	{
		String line;
		br1 = new BufferedReader(new FileReader(stopwords));
		
		while((line = br1.readLine()) != null)
        {
        	line.replaceAll("\\s+","");
        	if(listOfWords.containsKey(line))
        	{
        		listOfWords.remove(line);
        	}
        	if(doclist.containsKey(line))
        	{
        		doclist.remove(line);
        	}
        }
		br1.close();
	}
	
	/*
	 * Create the dictionary
	 */
	public static float Dictionary()
	{
		float start_time = System.nanoTime();
		for(String key : listOfWords.keySet())
		{
			Values val = new Values(doc_count.get(key), listOfWords.get(key));
			dictionary.put(key, val);
		}
		float end_time = System.nanoTime();
		return end_time - start_time;
	}
	
	/*
	 * Prints the dictionary
	 */
	public static void printDictionary() throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writer = new PrintWriter("./Results/Dictionary.txt", "UTF-8");
		
		for(String key : dictionary.keySet())
		{
			Values val = dictionary.get(key);
			//System.out.print(key);	
			writer.println(key);
			val.print(writer);
			//System.out.println("");
		}
		writer.close();
	}
	
	/*
	 * Create the posting
	 */
	public static float Posting()
	{
		float start_time = System.nanoTime();
		Values value = null;
		
		for(String key : dictionary.keySet())
		{
			value = dictionary.get(key);
			ArrayList<PostingsValues> pv = new ArrayList<PostingsValues>();
			TreeMap<String,Integer> temp = new TreeMap<String,Integer>();
			
			if(value.doc_freq == 1)
			{
				String doc_id = doclist.get(key).get(0);
				ArrayList<Integer> freq = new ArrayList<Integer>();
				
				temp = term_count.get(doc_id);
				freq.add(temp.get(key));
				
				PostingsValues val = new PostingsValues(doclist.get(key), freq);
				pv.add(val);
				postings.put(key, pv);
			}
			else
			{
				ArrayList<String> doc_id = doclist.get(key);
				ArrayList<Integer> freq = new ArrayList<Integer>();
				
				for(String doc : doc_id)
				{
					temp = term_count.get(doc);
					freq.add(temp.get(key));
				}
				
				PostingsValues val = new PostingsValues(doclist.get(key), freq);
				pv.add(val);
				postings.put(key, pv);
			}
		}
		float end_time = System.nanoTime();
		return end_time - start_time;
	}
	
	/*
	 * Print the postings
	 */
	public static void printPostings() throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writer = new PrintWriter("./Results/Postings.txt", "UTF-8");
		
		for(String key : postings.keySet())
		{
			ArrayList<PostingsValues> pv = postings.get(key);
			//System.out.print(key);
			writer.println(key);
			for(PostingsValues val : pv)
			{
				val.print(writer);
				//System.out.println("");
			}
		}
		writer.close();
	}
	
	/*
	 * Function to generate gamma code.
	 */
	public static String gamma(int number)
	{
		if(number == 1)
		{
			return "0";
		}
		
        else
        {
        	String gamma;
        	String binary = Integer.toBinaryString(number);
        	String substr = binary.substring(1);
        	int len = substr.length();
        	String temp = "";
        	
        	for(int i=0;i<len ;i++)
        	{
        		temp += "1";
        	}
        	
        	temp += "0";
        	gamma = temp + substr;
        	return gamma;
        }	
	}
	
	/*
	 * Function to generate Delta code.
	 */
	public static String delta(int number)
	{
		if(number == 1)
		{
            return "0";
		}
		
        else
        {
        	String binary = Integer.toBinaryString(number);
        	int len = binary.length();
        	String substr= binary.substring(1);
        	String DtoG = gamma(len);
        	String delta = DtoG + substr;
        	
        	return delta;                
        }
	}
	
	/*
	 * Creates compressed postings.
	 */
	public static float compressedPostings()
	{
		float start_time = System.nanoTime();
		
		Values value = null;
		
		for(String key : postings.keySet())
		{
			value = dictionary.get(key);
			ArrayList<PostingsValues> pv = new ArrayList<PostingsValues>();
			TreeMap<String,Integer> temp = new TreeMap<String,Integer>();
			
			if(value.doc_freq == 1)
			{
				String doc_id = doclist.get(key).get(0);
				ArrayList<String> freq = new ArrayList<String>();
				ArrayList<String> dist = new ArrayList<String>();
				
				temp = term_count.get(doc_id);
				freq.add(gamma(temp.get(key)));
				dist.add(doc_id.replaceAll("[a-zA-Z]", ""));
				
				PostingsValues val = new PostingsValues();
				val.Post(dist, freq);
				pv.add(val);
				compressed_postings.put(key, pv);
			}
			else
			{
				ArrayList<String> doc_id = doclist.get(key);
				ArrayList<String> freq = new ArrayList<String>();
				ArrayList<String> dist = new ArrayList<String>();
				
				for(String doc : doc_id)
				{
					temp = term_count.get(doc);
					freq.add(gamma(temp.get(key)));
				}
				dist.add(doc_id.get(0).replaceAll("[a-zA-Z]", ""));
				for(int i=1;i<doc_id.size();i++)
				{
					dist.add(delta(docDistance(doc_id.get(i-1), doc_id.get(i))));
				}
				
				
				PostingsValues val = new PostingsValues();
				val.Post(dist, freq);
				pv.add(val);
				compressed_postings.put(key, pv);
			}
		}
		float end_time = System.nanoTime();
		return end_time - start_time;
	}
	
	/*
	 * Prints compressed postings
	 */
	public static void printCompressedPostings() throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writer = new PrintWriter("./Results/CompressedPostings.txt", "UTF-8");
		
		for(String key : compressed_postings.keySet())
		{
			ArrayList<PostingsValues> pv = compressed_postings.get(key);
			//System.out.print(key);
			writer.println(key);
			
			for(PostingsValues val : pv)
			{
				val.printCompressed(writer);
				//System.out.println("");
			}
		}
		writer.close();
	}
	
	/*
	 * Gets the difference between two documents
	 */
	public static int docDistance(String doc1, String doc2)
	{
		String d1 = doc1.replaceAll("[a-zA-Z]", "");
		String d2 = doc2.replaceAll("[a-zA-Z]", "");
		return Integer.parseInt(d2)-Integer.parseInt(d1);
	}
	
	/*
	 * Creates the index by combining the dictionary and postings
	 */
	public static float createIndex()
	{
		float start_time = System.nanoTime();
		for(String key : dictionary.keySet())
		{
			HashMap<Values, ArrayList<PostingsValues>> tm = new HashMap<Values, ArrayList<PostingsValues>>();
			ArrayList<PostingsValues> pv = new ArrayList<PostingsValues>();
			
			for(PostingsValues p : postings.get(key))
			{
				pv.add(p);
			}
			tm.put(dictionary.get(key), pv);
			index.put(key, tm);
		}
		float end_time = System.nanoTime();
		return end_time - start_time;
	}
	
	/*
	 * Prints the creates index
	 */
	public static void printIndex() throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter write = new PrintWriter("./FinalIndices/Index.txt", "UTF-8");
		for(String key : index.keySet())
		{
			ArrayList<PostingsValues> pv = postings.get(key);
			//System.out.println(key);
			write.println(key);
			
			Values v = dictionary.get(key);
			v.print(write);
			
			for(PostingsValues val : pv)
			{
				val.print(write);
				//System.out.println("");
			}
		}
	}
	
	/*
	 * Creates compressed index.
	 */
	public static float createCompressedIndex()
	{
		float start_time = System.nanoTime();
		for(String key : dictionary.keySet())
		{
			HashMap<Values, ArrayList<PostingsValues>> tm = new HashMap<Values, ArrayList<PostingsValues>>();
			ArrayList<PostingsValues> pv = new ArrayList<PostingsValues>();
			
			for(PostingsValues p : compressed_postings.get(key))
			{
				pv.add(p);
			}
			tm.put(dictionary.get(key), pv);
			compressed_index.put(key, tm);
		}
		float end_time = System.nanoTime();
		return end_time - start_time;
	}
	
	/*
	 * Prints compressed index.
	 */
	public static void printCompressedIndex() throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter write = new PrintWriter("./FinalIndices/CompressedIndex.txt", "UTF-8");
		for(String key : compressed_index.keySet())
		{
			ArrayList<PostingsValues> pv = compressed_postings.get(key);
			//System.out.println(key);
			write.println(key);
			
			Values v = dictionary.get(key);
			v.print(write);
			
			for(PostingsValues val : pv)
			{
				val.printCompressed(write);
				//System.out.println("");
			}
		}
	}
	
	/*
	 * Size of uncompressed index.
	 */
	public static void sizeOfUncompressedIndex()
	{	
		int size = 0;
		for(String key : postings.keySet())
		{
			ArrayList<PostingsValues> pv = postings.get(key);
			
			for(PostingsValues v : pv)
			{
				for(String s : v.doc_no)
				{
					size += s.getBytes().length;
				}
				for(int s : v.freq)
				{
					size += BigInteger.valueOf(s).toByteArray().length;
				}
			}
		}
		for(String key : dictionary.keySet())
		{
			Values val = dictionary.get(key);
			
			size += BigInteger.valueOf(val.doc_freq).toByteArray().length + BigInteger.valueOf(val.total_freq).toByteArray().length + key.getBytes().length;
		}
		System.out.println("Size of uncompressed list " + size + " Bytes");
	}
	
	/*
	 * Size of compressed index.
	 */
	public static void sizeOfCompressedIndex()
	{
		int size = 0;
		for(String key : compressed_postings.keySet())
		{
			ArrayList<PostingsValues> pv = compressed_postings.get(key);
			
			for(PostingsValues v : pv)
			{
				for(String s : v.doc_no)
				{
					size += BigInteger.valueOf(s.length()).toByteArray().length;
				}
				for(String s : v.gamma_freq)
				{
					size += BigInteger.valueOf(s.length()).toByteArray().length;
				}
			}
		}
		for(String key : dictionary.keySet())
		{
			Values val = dictionary.get(key);
			
			size += BigInteger.valueOf(val.doc_freq).toByteArray().length + BigInteger.valueOf(val.total_freq).toByteArray().length + key.getBytes().length;
		}
		System.out.println("Size of compressed list " + size + " Bytes");
	}
	
	/*
	 * Number of inverted lists in index
	 */
	public static void NumberofInvertedLists()
	{
		int count = 0;
		
		for(String key : index.keySet())
		{
			count++;
		}
		System.out.println("The number of inverted lists in the index is " + count);
	}
	
	/*
	 * Returns df of term
	 */
	public static int returnDf(String term)
	{
		Values val = null;
		for(String key : dictionary.keySet())
		{
			if(key.equals(term))
			{
				val = dictionary.get(key); 
				break;
			}
		}
		return val.doc_freq;
	}
	
	/*
	 * Returns tf of term
	 */
	public static int returnTf(String term)
	{
		Values val = null;
		for(String key : dictionary.keySet())
		{
			if(key.equals(term))
			{
				val = dictionary.get(key); 
				break;
			}
		}
		return val.total_freq;
	}
	
	/*
	 * Returns inverted list length in bytes for a term
	 */
	public static int returnInvertedListLength(String term)
	{
		int size = 0;
		
		for(String key : compressed_postings.keySet())
		{
			if(term.equals(key))
			{
				ArrayList<PostingsValues> pv = compressed_postings.get(key);
				
				for(PostingsValues v : pv)
				{
					for(String s : v.doc_no)
					{
						size += BigInteger.valueOf(s.length()).toByteArray().length;
					}
					for(String s : v.gamma_freq)
					{
						size += BigInteger.valueOf(s.length()).toByteArray().length;
					}
				}
			}
		}
		for(String key : dictionary.keySet())
		{
			if(term.equals(key))
			{
				Values val = dictionary.get(key);
			
				size += BigInteger.valueOf(val.doc_freq).toByteArray().length + BigInteger.valueOf(val.total_freq).toByteArray().length + key.getBytes().length;
			}
		}
		
		return size;
	}
}

class Values
{
	int doc_freq;
	int total_freq;
	
	public Values(int doc_freq, int term_freq)
	{
		this.doc_freq = doc_freq;
		this.total_freq = term_freq;
		
	}
	public Values()
	{
		doc_freq = 0;
		total_freq = 0;
	}
	public void print(PrintWriter writer)
	{
		//System.out.print(" " + doc_freq);
		//System.out.print(" " + total_freq);
		writer.println(" " + doc_freq);
		writer.println(" " + total_freq);
	}
}

class PostingsValues
{
	ArrayList<String> doc_no;
	ArrayList<Integer> freq;
	ArrayList<String> gamma_freq;
	
	public PostingsValues(ArrayList<String> doc_no, ArrayList<Integer> freq)
	{
		this.doc_no = doc_no;
		this.freq = freq;
	}
	public void Post(ArrayList<String> doc_no, ArrayList<String> freq)
	{
		this.doc_no = doc_no;
		this.gamma_freq = freq;
	}
	public PostingsValues()
	{
		doc_no = null;
		freq = null;
		gamma_freq = null;
	}
	public void print(PrintWriter writer)
	{
		int i = 0;
		
		for(String doc : doc_no)
		{
			//System.out.println("");
			//writer.println("");
			//System.out.print(" " + doc_no.get(i));
			writer.println(" " + doc_no.get(i));
			//System.out.print(" " + freq.get(i));
			writer.println(" " + freq.get(i));
			//writer.println("");
			//System.out.println("");
			i++;
		}
	}
	public void printCompressed(PrintWriter writer)
	{
		int i = 0;
		
		for(String doc : doc_no)
		{
			//System.out.println("");
			//writer.println("");
			//System.out.print(" " + doc_no.get(i));
			writer.println(" " + doc_no.get(i));
			//System.out.print(" " + gamma_freq.get(i));
			writer.println(" " + gamma_freq.get(i));
			//writer.println("");
			//System.out.println("");
			
			i++;
		}
	}
}