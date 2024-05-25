package com.cdac.esign.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

public class FieldValidator {

	public boolean validateFields(String aadhar, String consent, String authType, MultipartFile[] fileData) {

		boolean validateFlag = true;   
		/*if(aadhar != null) {
			if(aadhar.equals("")) {
				   validateFlag = false;
			   }
			else if(aadhar.length()!=12) {
				validateFlag = false;
			}else {
				Pattern testPattern= Pattern.compile("^[0-9]{12}$");
				Matcher teststring= testPattern.matcher(aadhar);
	
				if(!teststring.matches())
				{
					validateFlag = false;
				}
				else
				{
					validateFlag = validateAadharNumber(aadhar);
				}
			}
		}else {
			validateFlag = false;
		}*/
		if(consent != null) {
				if(!consent.equals("Y")){
				validateFlag = false;
			}
		}else {
			validateFlag = false;
		}
		if(authType != null) {
			if(!(authType.equals("1") || authType.equals("2"))){
				validateFlag = false;
			}
		}else {
			validateFlag = false;
		}
		if(fileData.length == 0) {
			validateFlag = false;
		}
		   return validateFlag;
	   }
	//Aadhar validation 
	static int[][] d  = new int[][]
            {
                    {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                    {1, 2, 3, 4, 0, 6, 7, 8, 9, 5},
                    {2, 3, 4, 0, 1, 7, 8, 9, 5, 6},
                    {3, 4, 0, 1, 2, 8, 9, 5, 6, 7},
                    {4, 0, 1, 2, 3, 9, 5, 6, 7, 8},
                    {5, 9, 8, 7, 6, 0, 4, 3, 2, 1},
                    {6, 5, 9, 8, 7, 1, 0, 4, 3, 2},
                    {7, 6, 5, 9, 8, 2, 1, 0, 4, 3},
                    {8, 7, 6, 5, 9, 3, 2, 1, 0, 4},
                    {9, 8, 7, 6, 5, 4, 3, 2, 1, 0}
            };
    static int[][] p = new int[][]
            {
                    {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                    {1, 5, 7, 6, 2, 8, 3, 0, 9, 4},
                    {5, 8, 0, 3, 7, 9, 6, 1, 4, 2},
                    {8, 9, 1, 6, 0, 4, 3, 5, 2, 7},
                    {9, 4, 5, 3, 1, 2, 6, 8, 7, 0},
                    {4, 2, 8, 6, 5, 7, 3, 9, 0, 1},
                    {2, 7, 9, 3, 8, 0, 6, 4, 1, 5},
                    {7, 0, 4, 6, 9, 1, 3, 2, 5, 8}
            };
    static int[] inv = {0, 4, 3, 2, 1, 5, 6, 7, 8, 9};

    public static boolean validateVerhoeff(String num){
        int c = 0;
        int[] myArray = StringToReversedIntArray(num);
        for (int i = 0; i < myArray.length; i++){
            c = d[c][p[(i % 8)][myArray[i]]];
        }

        return (c == 0);
    }
    private static int[] StringToReversedIntArray(String num){
        int[] myArray = new int[num.length()];
        for(int i = 0; i < num.length(); i++){
            myArray[i] = Integer.parseInt(num.substring(i, i + 1));
        }
        myArray = Reverse(myArray);
        return myArray;
    }
    private static int[] Reverse(int[] myArray){
        int[] reversed = new int[myArray.length];
        for(int i = 0; i < myArray.length ; i++){
            reversed[i] = myArray[myArray.length - (i + 1)];
        }
        return reversed;
    }
    
    public static boolean validateAadharNumber(String aadharNumber){
        Pattern aadharPattern = Pattern.compile("\\d{12}");
        boolean isValidAadhar = aadharPattern.matcher(aadharNumber).matches();
        if(isValidAadhar){
            //isValidAadhar = VerhoeffAlgorithm.validateVerhoeff(aadharNumber);
        	isValidAadhar = validateVerhoeff(aadharNumber);
        }
        return isValidAadhar;
    }
}
