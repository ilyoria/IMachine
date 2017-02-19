/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imachine;

/**
 *
 * @author ilyes
 */
public interface OperationCodes
{      
    
    /**
     * asignments 
     */
    int BYTE   = 1;
    int SHORT  = 2;
    int CHAR   = 3;
    int INT    = 4;
    int LONG   = 5;
    int FLOAT  = 6;
    int DOUBLE = 7;
    int CHARS  = 8;
    
    // Input/Output operations:
    
    /**
     * Input operations: Read a word from the keyboard into a specific location in memory.
     */
    int READ = 10; 
    
    /**
     * output operations: Write a word from a specific location in memory to the screen.
     */
    int WRITE = 11;
    
    /**
     * write new line
     */
    int WRITENL = 12;
    
    // Load/store operations:
    
    /**
     * Load operation: Load a word from a specific location in memory into the accumulator.
     */
    int LOAD = 20;
    /**
     * Store operation: Store a word from the accumulator into a specific location in memory.
     */
    int STORE = 21;
    
    // Transfer-of-control operations:
    
    /**
     * Branch to a specific location in memory.
     */
    int BRANCH = 40;
    
    /**
     * Branch to a specific location in memory if the accumulator is negative.
     */
    int BRANCHNEG = 41;
    
    /**
     * Branch to a specific location in memory if the accumulator is zero.
     */
    int BRANCHZERO = 42;
    
    /**
     * Halt. The program has completed its task.
     */
    int HALT = 43;
    
    // Arithmetic operations:
    
    /**
     * Add a word from a specific location in memory to the word in the
     * accumulator (leave the result in the accumulator).
     */
    int ADD = 30;
    
    /**
     * Subtract a word from a specific location in memory from the word in
     * the accumulator (leave the result in the accumulator).
     */
    int SUB = 31;
    
    /**
     * Divide a word from a specific location in memory into the word in
     * the accumulator (leave result in the accumulator).
     */
    int DIV = 32;
    
    /**
     * Multiply a word from a specific location in memory by the word in
     * the accumulator (leave the result in the accumulator).
     */
    int MUL = 33;
    
    /**
     * modulos
     */
    int MOD = 34;
    
    /**
     * increment
     */
    int INC = 35;
    
    /**
     * decrement
     */
    int DEC = 36;
    
    /**
     * exponent
     */
    int EXP = 50;
    
    /**
     * type of calculation
     */
    int CALTYPE = 51;
    
    /**
     * the precision of float and double
     */
    int PRC = 52;
    
    /**
     * square root
     */
    int SQRT = 53;
    
    /**
     * Label  
     */
    int LABEL = 70;
    
    /**
     * procedure
     */
    int PROCEDURE = 71;
    
    /**
     * return
     */
    int RETURN = 72;
    
    /**
     * void
     */
    int CALL = 73;
    
    // other: 
    
    byte END_OF_PROCESS = -1;
    
    // Ilyes Machine Assembly:
    
    String BYTE_IMA   = "ثماني";
    String SHORT_IMA  = "قصير";
    String CHAR_IMA   = "حرفي";
    String INT_IMA    = "صحيح";
    String LONG_IMA   = "طويل";
    String FLOAT_IMA  = "عائم";
    String DOUBLE_IMA = "مزدوج";
    String CHARS_IMA  = "نصي";
    
    String READ_IMA    = "اقرا";

    String WRITE_IMA     = "اكتب";
    String WRITENL_IMA    = "اكتبسج";
    
    String LOAD_IMA   = "حمل";
    
    String STORE_IMA   = "خزن";
    
    String ADD_IMA  = "جمع";
    String SUB_IMA  = "طرح";
    String DIV_IMA  = "قسمة";
    String MUL_IMA  = "ضرب";
    
    String BRANCH_IMA     = "تقاطع";
    String BRANCHNEG_IMA   = "تقاطعسلب";    
    String BRANCHZERO_IMA = "تقاطعصفر";
    
    String HALT_IMA = "انهاء";
    
    String MOD_IMA = "باقي";
    String EXP_IMA = "اس";
    
    String CALTYPE_IMA = "نوع";
    
    String INC_IMA = "زد";   
    String DEC_IMA = "نقص";
    
    String PRC_IMA = "دقة";
    
    String SQRT_IMA = "جذر";
    
    String LABEL_IMA = "ملصق";
    
    String PROCEDURE_IMA = "اجراء";
    
    String RETURN_IMA = "رجوع";
    
    String CALL_IMA = "استدعاء";
     
} // end interface OperationCodes
