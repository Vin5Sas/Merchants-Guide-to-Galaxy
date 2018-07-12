package merchantspeaks;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.util.regex.Pattern;

class StringOperations
{
    public static String removeIS(String input)
    {
        return input.replaceAll(" is "," ");        //removes the occurence of " is " in the String
    }
    public static String removeRedundantSpaces(String input)
    {
        return input.replaceAll("(\\s)\\1+"," ");   //removes redundant spaces present in the String
    }
    public static String removeMuchQuestion(String input)
    {
        return input.replaceAll("(?i)how much", "").replaceAll("\\?", "");  //removes "how much" and '?' it the String
    }
    public static String removeManyQuestion(String input)
    {
        return input.replaceAll("(?i)how many", "").replaceAll(" (?i)credits"," ").replaceAll("\\?", "");   //removes "how many" "credits" and '?' in the String
    }
    public static String trimString(String input)
    {
        return input.trim();
    }
    public static String extractQuantityString(String input)
    {
        String quantityString = "";                   //can contain only quantity represented in User defined keys
        Pattern p = Pattern.compile("\\s");
        String resultSet[] = p.split(input);  //splits commodity string into separate words and stores in an array
    
        for (String temp : resultSet)                   //iterates through array containing split words
        {
            for(Map.Entry m:UserLegend.userKeys.entrySet()) //iterates User Legend Hash Map
            {
                //System.out.println("Comparing " + temp + " and " + m.getKey().toString());
                if (temp.equalsIgnoreCase(m.getKey().toString()))   //checks if the split words are present in the UserLegend HashMap
                {
                    quantityString += temp + " ";           //if Yes, add it to the quantityString after appending space
                }
            }
        }
        quantityString = quantityString.trim();
        return quantityString;
    }
    
    public static String processString(String input)
    {   
        if(input.contains(" much "))
            input = removeMuchQuestion(input);
        if(input.contains(" many "))
            input = removeManyQuestion(input);
        input = removeIS(input);
        input = removeRedundantSpaces(input);
        input = trimString(input);
        
        return input;
    }
}

class Rules
{
    public static boolean IOrder(int index, String input[])      //'I' can be subtracted only from 'V' or 'X'
    {
        boolean orderProper = false;
        
        if(UserLegend.userKeys.get(input[index+1])==5 || UserLegend.userKeys.get(input[index+1])==10)   //the next word should either be 'V'(5) or 'X'(10)
            orderProper = true;
        
        return orderProper;
    }
    public static boolean XOrder(int index, String input[])      //'X' can be subtracted only from 'L' or 'C'
    {
        boolean orderProper = false;
        
        if(UserLegend.userKeys.get(input[index+1])==50 || UserLegend.userKeys.get(input[index+1])==100)   //the next word should either be 'V'(5) or 'X'(10)
            orderProper = true;
        
        return orderProper;
    }
    public static boolean COrder(int index, String input[])      //'C' can be subtracted only from 'D' or 'M'
    {
        boolean orderProper = false;
        
        if(UserLegend.userKeys.get(input[index+1])==500 || UserLegend.userKeys.get(input[index+1])==1000)   //the next word should either be 'V'(5) or 'X'(10)
            orderProper = true;
        
        return orderProper;
    }
    
    public static boolean checkSubtractionOrder(String quantityString)
    {
        boolean isProper = true;
        Pattern p = Pattern.compile("\\s");
        String resultSet[] = p.split(quantityString);
        
        for(int i=0; i<(resultSet.length)-1; i++)
        {
            if(UserLegend.userKeys.get(resultSet[i])<UserLegend.userKeys.get(resultSet[i+1]))    //if word mathces userKeys
            {   
                    if(UserLegend.userKeys.get(resultSet[i])==1)
                        isProper = IOrder(i,resultSet);
                    if(UserLegend.userKeys.get(resultSet[i])==10)
                        isProper = XOrder(i,resultSet);    
                    if(UserLegend.userKeys.get(resultSet[i])==100)
                        isProper = COrder(i,resultSet);
                    i++;
                    //break;
            }
            if(isProper==false) break;
        }
        
        return isProper;
    }
    
    public static boolean checkRepetition(String quantityString)
    {
        boolean repeatOrder = true;
        int repeatCount = 1;
        
        Pattern p = Pattern.compile("\\s");
        String resultSet[] = p.split(quantityString);
        
        for(int i=0; i<(resultSet.length)-1; i++)
        {
            for(int j=i+1; j<=(resultSet.length)-1; j++)
            {    
                if(resultSet[i].equalsIgnoreCase(resultSet[j]))
                {
                    repeatCount++;
                }
                else                    //if (XXX I X) reset Count of X to 1
                    repeatCount = 1;
                if(repeatCount>3)
                {
                    repeatOrder = false;
                    break;
                }
            }
            repeatCount = 1;
            if(repeatOrder==false)
                break;
        }
        
        return repeatOrder;
    }
    
    public static boolean validString(String input)
    {
        boolean stringIsValid = true;
        input = StringOperations.extractQuantityString(input);  //extracts quantities alone from commodityString
        stringIsValid = checkRepetition(input);
        if(stringIsValid==true) //after checking for repetitions
            stringIsValid = checkSubtractionOrder(input);
        return stringIsValid;
    }
}

class RomanLegend
{
    public static HashMap<Character,Integer> romanNumerals = new HashMap<Character,Integer>();
    
    RomanLegend()
    {
        romanNumerals.put('I', 1);
        romanNumerals.put('V', 5);
        romanNumerals.put('X', 10);
        romanNumerals.put('L', 50);
        romanNumerals.put('C', 100);
        romanNumerals.put('D', 500);
        romanNumerals.put('M', 1000);
    }
}

class Quantity
{
    public static int returnCheckedOrder(String quantityString)     //evaluates the quantity by adding values of the keys in correct order
    {
        int quantity = 0;
        int max,min;
        Pattern p = Pattern.compile("\\s");
        String tempArray[] = p.split(quantityString);   //split quantityString into separate words and store in tempArray
        
        /*for(int i=0; i<(tempArray.length); i++)
        {
           System.out.print(tempArray[i] + " ");
        }*/
        
        for(int i=0; i<=(tempArray.length)-1; i++)       //traverses till second last element of tempArray [p t g g]
        {
            if(i==((tempArray.length)-1))                           //adds value of last quantity
                quantity+= UserLegend.userKeys.get(tempArray[i]);
            else if(UserLegend.userKeys.get(tempArray[i])<UserLegend.userKeys.get(tempArray[i+1]))   //checks if preceding number is less than following number
            {   
                max = UserLegend.userKeys.get(tempArray[i+1]);
                min = UserLegend.userKeys.get(tempArray[i]);
                quantity+=(max-min);
                i++;
            }
            else
                quantity+= UserLegend.userKeys.get(tempArray[i]);
        }
        
        return quantity;
    }
}

class UserLegend
{
    public static HashMap<String,Integer> userKeys = new HashMap<String,Integer>();
    
    public void storeKeyAndValue(String userKeyString)
    {
        userKeyString = userKeyString.replaceAll(" is "," ");        //removes "is" from the given String
        userKeyString = userKeyString.replaceAll("(\\s)\\1+"," ");  //removes repetition of 'space'; (1+) means more than once
        
        Pattern p = Pattern.compile("\\s");
        String resultSet[] = p.split(userKeyString);                //splits the userKeyString into two (User defined key AND equivalent Roman Letter)
        //resultSet[0] contains userdefined key
        //resultSet[1] contains Roman Letter
        
        char romanLetter = resultSet[1].toUpperCase().charAt(0);                  //convert String to char                 
        //System.out.println("Extracted Roman Letter is: " + romanLetter);
        //System.out.println("Roman Letter is: " + romanLetter);
        //System.out.println("Extracted Roman Letter equivalent is: " + RomanLegend.romanNumerals.get(romanLetter));
        userKeys.put(resultSet[0], RomanLegend.romanNumerals.get(romanLetter));  //add entry to userKeys HashMap
        System.out.println(resultSet[0] + " " + RomanLegend.romanNumerals.get(romanLetter) + " stored in userKeys HashMap");
    }
}

class Commodity
{
    public static HashMap<String,Float> commodityList = new HashMap<String,Float>();
    String commodityName;
    float quantity;
    float unitCredit;
    
    public static float extractQuantity(String commodityString)
    {
        float extractedQuantity;
        String quantityString = "";                   //can contain only quantity represented in User defined keys
        Pattern p = Pattern.compile("\\s");
        String resultSet[] = p.split(commodityString);  //splits commodity string into separate words and stores in an array
    
        /*for (String temp : resultSet)                   //iterates through array containing split words
        {
            for(Map.Entry m:UserLegend.userKeys.entrySet()) //iterates User Legend Hash Map
            {
                //System.out.println("Comparing " + temp + " and " + m.getKey().toString());
                if (temp.equalsIgnoreCase(m.getKey().toString()))   //checks if the split words are present in the UserLegend HashMap
                {
                    quantityString += temp + " ";           //if Yes, add it to the quantityString after appending space
                }
            }
        }*/
        quantityString = StringOperations.extractQuantityString(commodityString);
        //System.out.println("Extracted Quantity String is: " + quantityString);
        extractedQuantity = Quantity.returnCheckedOrder(quantityString);
        //System.out.println("Quantity computed is: " + quantity);
        return extractedQuantity;
    }
    public float extractUnitCredit(String commodityString)
    {
        String creditString = commodityString.replaceAll("\\D",""); //  \\D is equivalent to [^0-9] (non-digit) - replaces all strings except digits
        int givenCredit = Integer.parseInt(creditString);
        //System.out.println("Given credit is: " + givenCredit);
        //unitCredit = givenCredit/quantity;
        return (givenCredit/quantity);
        //System.out.println("Unit credit is: " + unitCredit);
    }
    public static String extractCommodityName(String commodityString)
    {
        //commodityString = commodityString.toLowerCase();    //converts commodity to LoweCase
        String extractedName="";
        commodityString = commodityString.replaceAll("\\d", "");    //  \\d is equivalent to [0-9] (digit) - replaces all digit occurences with ""
        //commodityString = commodityString.replace(" is "," ");      //replaces " is " with " "
        commodityString = commodityString.replaceAll(" (?i)credits"," ");  //(?i) is case-insensitive
        //commodityString = commodityString.replaceAll("(\\s)\\1+"," ");  //replaces redundant spaces
        commodityString = StringOperations.processString(commodityString);
        Pattern p = Pattern.compile("\\s");
        String resultSet[] = p.split(commodityString);  //splits commodity string into separate words and stores in an array
        
        for (String temp : resultSet)                   //iterates through array containing split words
        {
            for(Map.Entry m:UserLegend.userKeys.entrySet()) //iterates User Legend Hash Map
            {
                //System.out.println("Comparing " + temp + " and " + m.getKey().toString());
                if (!temp.equalsIgnoreCase(m.getKey().toString()))   //checks if the split words are present in the UserLegend HashMap
                {
                    //commodityName = temp;                        
                    extractedName = temp;
                }
            }
        }
        //System.out.println("Commodity Name is: " + commodityName);
        return extractedName;
    }
    
    public void addCommodity(String input)
    {
        quantity = extractQuantity(input); 
        unitCredit = extractUnitCredit(input);
        commodityName = extractCommodityName(input);
        
        commodityList.put(commodityName, unitCredit);
        System.out.println(commodityName + ' ' + unitCredit + " added to Commodity HashMap!");
    }
}

class QueryHandler
{
    int requiredQuanity;
    
    public boolean checkValidity(String query)
    {
        boolean matchNotFound = false;
        boolean matchFound = false;
                
        Pattern p = Pattern.compile("\\s");
        String resultSet[] = p.split(query);
        
        for (String temp : resultSet)                   //iterates through array containing split words
        {
            for(Map.Entry m:UserLegend.userKeys.entrySet()) //iterates User Legend Hash Map
            {
                matchFound = false;
                //System.out.println("Comparing " + temp + " and " + m.getKey().toString());
                if (temp.equalsIgnoreCase(m.getKey().toString()))   //checks if the split words are NOT present in the UserLegend HashMap
                {
                    //matchNotFound = true;                            //makes 'matchNotFound' = true if new word is encountered                   
                    matchFound = true;
                }
                if(matchFound==true)
                    break;
            }
            /*if(matchNotFound==true)                                  //exits loop if any new word is encountered
                break;*/
            if(matchFound==false)
                break;
        } 
        
        //return !(matchNotFound);                                    //returns 'false' if matchNotFound
        return matchFound;
    }
    public void muchQuery(String query)     //used for adding User Keys
    {
        int extractedQuantity;
        String modifiedQuery = query;
        
        //modifiedQuery = modifiedQuery.replaceAll("(?i)how much", "");   //removes "how much" from query
        //modifiedQuery = modifiedQuery.replaceAll(" is "," ");           //removes " is " from query
        //modifiedQuery = modifiedQuery.replaceAll("\\?", "");              //removes "?" from query
        //modifiedQuery = modifiedQuery.replaceAll("(\\s)\\1+"," ");      //removes redundant spaces
        //modifiedQuery = modifiedQuery.trim();
        query = StringOperations.processString(query);
        //System.out.println("Extracted Query is: " + modifiedQuery);
        System.out.println("Extracted Query is: " + query);
        /*if(checkValidity(modifiedQuery)==true)        
        {    
            extractedQuantity = Quantity.returnCheckedOrder(modifiedQuery);
            System.out.println(modifiedQuery + " is " + extractedQuantity);
        }*/
        if(checkValidity(query)==true)
        {    
            extractedQuantity = Quantity.returnCheckedOrder(query);
            System.out.println(query + " is " + extractedQuantity);
        }
        else
            System.out.println("You are speaking Gibberish!");
    }
    public void manyQuery(String query)     //used for handling Queries on Commodities
    {
        String modifiedQuery = query;
        String commodityGiven;
        int quantityGiven;
        float computedCredits;
        
        //modifiedQuery = modifiedQuery.replaceAll("(?i)how many", "");   //removes "how many" from query
        //modifiedQuery = modifiedQuery.replaceAll(" (?i)credits"," ");   //removes "credits" from query
        //modifiedQuery = modifiedQuery.replaceAll(" is "," ");
        //modifiedQuery = modifiedQuery.replaceAll("\\?", "");              //removes "?" from query
        //modifiedQuery = modifiedQuery.replaceAll("(\\s)\\1+"," ");      //removes redundant spaces
        //modifiedQuery = modifiedQuery.trim();
        query = StringOperations.processString(query);
        //System.out.println("Extracted Query is: " + modifiedQuery);
        System.out.println("Extracted Query is: " + query);
        //commodityGiven = Commodity.extractCommodityName(modifiedQuery);
        commodityGiven = Commodity.extractCommodityName(query);
        System.out.println("Commodity given in Query is: " + commodityGiven);
        
        //quantityGiven = (int) Commodity.extractQuantity(modifiedQuery);
        quantityGiven = (int) Commodity.extractQuantity(query);
        System.out.println("Quantity given in Query is: " + quantityGiven);
        
        computedCredits = quantityGiven*(Commodity.commodityList.get(commodityGiven));
        //System.out.println(modifiedQuery + " is " + computedCredits);
        System.out.println(query + " is " + computedCredits);
    }
    public void decideQuery(String query)
    {
        if(query.contains(" much "))
            muchQuery(query);
        else if(query.contains(" many "))
            manyQuery(query);
        else
            System.out.println("Question is ambiguous!");
    }
}

public class MerchantSpeaks {

    public static void main(String[] args) {
     
        RomanLegend romanLegend = new RomanLegend();
        UserLegend userLegend = new UserLegend();
        Commodity commodity = new Commodity();
        QueryHandler queryHandler = new QueryHandler();
        
        Scanner in = new Scanner(System.in);
        
        String inputString;
        char choice = 'Y';
        
        /*  UserKeys testcase 
        romanLegend = new RomanLegend();
        userLegend = new UserLegend();
        userLegend.storeKeyAndValue("glob is I");
        userLegend.storeKeyAndValue("prok is V");
        userLegend.storeKeyAndValue("pish is X ");
        userLegend.storeKeyAndValue("tegj is L");
        */
        
        /*  Commodity Test Case
        commodity = new Commodity();
        //commodity.extractQuantity("glob glob prok Silver is 34 Credits ");
        //commodity.extractQuantity("pish tegj glob glob is 42 ");
        //commodity.extractQuantity("glob prok Gold is 57800 Credits "); 
        //commodity.extractUnitCredit("glob prok Gold is 57800 Credits ");
        //commodity.extractCommodityName("glob prok Gold is 57800 Credits ");
        commodity.addCommodity("glob glob Silver is 34 Credits");
        commodity.addCommodity("glob prok Gold is 57800 Credits"); 
        commodity.addCommodity("pish pish Iron is 3910 Credits ");
        */        
        /*  QuerHandler Test Case
        queryHandler = new QueryHandler();
        queryHandler.decideQuery("how much is pish tegj glob glob ? ");
        queryHandler.decideQuery("how many credits is pish tegj Gold?");
        queryHandler.decideQuery("how many credits is glob prok Iron ?");
        */
        
        System.out.println("Define InterGalactic Language: \n");
        while(choice!='N')    //Enter UserDefined Legend
        {
            System.out.println("Enter User Legend (Like <key> is <RomanLetter>)");
            inputString = in.nextLine();
            //System.out.println("InputString is: " + inputString);
            userLegend.storeKeyAndValue(inputString);
            System.out.println("Continue? (Y/N).....");
            choice = in.nextLine().toUpperCase().charAt(0);
            inputString="";
        }
        choice='Y';             //reset choice to 'Y'
        
        System.out.println("\nCommodity Details: \n");
        while(choice!='N')      //Enter Commodity Details
        {
            System.out.println("Enter Commodity Details (Like <Quantity...> <Commodity> is <CreditValue> Credits)");
            inputString = in.nextLine();
            if(Rules.validString(StringOperations.extractQuantityString(inputString))==true)
            {   
                commodity.addCommodity(inputString);
            }
            else
                System.out.println("String is Invalid");
            //commodity.addCommodity(inputString);
            System.out.println("Continue? (Y/N).....");
            choice = in.nextLine().toUpperCase().charAt(0);
        }
        choice='Y';             //reset choice to 'Y'
        
        System.out.println("\nQUERY Space: \n");
        while(choice!='N')
        {
            System.out.println("Enter Query (Like how (much)/(many Credits) is <quantity...> <commodity> ?)");
            inputString = in.nextLine();
            if(Rules.validString(StringOperations.extractQuantityString(inputString))==true)
            {   
                queryHandler.decideQuery(inputString);
            }
            else
                System.out.println("String is Invalid");
            //queryHandler.decideQuery(inputString);
            System.out.println("Continue? (Y/N).....");
            choice = in.nextLine().toUpperCase().charAt(0);
        }
        
        /*
        System.out.println("Enter a query: ");
        inputString = in.nextLine();
        inputString = StringOperations.removeIS(inputString);
        //inputString = StringOperations.removeManyQuestion(inputString);
        inputString = StringOperations.removeMuchQuestion(inputString);
        inputString = StringOperations.removeRedundantSpaces(inputString);
        inputString = StringOperations.trimString(inputString);
        System.out.println("Modified Query: " + inputString);
        */
        
        /*System.out.println("Enter a commodity string");
        inputString = in.nextLine();
        inputString = StringOperations.extractQuantityString(inputString);
        System.out.println(inputString);
        if(Rules.validString(inputString)==true)
            System.out.println("String is valid!");
        else
            System.out.println("String is Invalid");
        */
    }
    
}
