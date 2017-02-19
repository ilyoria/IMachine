package imachine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ilyes
 */
public class IMachine implements OperationCodes
{  
    /**
     * imachine file log output
     */
    private static PrintStream LOG_OUTPUT;
    
    /**
     * imachine file error output
     */
    private static PrintStream ERR_OUTPUT;
    
    /**
     * imachine state
     */
    private IMStates machineState = null;
    
    /**
     * specify the calculation type byte, short, int, long, float or double<br>
     * if calculationType == 10 then the second operand is byte<br>
     * if calculationType == 11 then the second operand is short<br>
     * if calculationType == 12 then the second operand is int<br>
     * if calculationType == 13 then the second operand is long<br>
     * if calculationType == 14 then the second operand is float<br>
     * if calculationType == 15 then the second operand is double<br>
     * you can alter it with the instruction <code>OperationCodes.CALTYPE</code><br>
     * like this instruction <code>+66 10<code>
     */
    private static int calculationType = 4;
    
    /**
     * handle user input
     */
    protected static Scanner keyboard = new Scanner( System.in );
    
    /**
     * the address size
     */
    protected final static int ADDRESS_SIZE; 
    
    /**
     * the instruction size
     */
    protected final static int INSTRUCTION_SIZE;
    
    
    //protected static boolean floatLoaded = false; 
    
    /**
     * the IMachine Accumulator
     */
    protected static double accum = 0;
    
    /**
     * IMachine main MEMORY
     */
    protected final static ByteBuffer MEMORY;
    
    /**
     * indicate the operation currently being performed<br>for example +10(READ8)
     */
    protected static int operationCode = 0;
    
    /**
     * indicate the MEMORY location
     * on which the current instruction operates
     */
    protected static int operand = 0;
    
    /**
     * next instruction to be performed from MEMORY
     */
    protected static int instructionRegister = 0;
    
    /**
     * keep track of the location in MEMORY that contains the instruction being performed
     */
    protected static int instructionCounter = 0;
    
    /**
     * hold the String that to be loaded by CHARS instruction
     */
    protected static String asignstr = null;
    
    /**
     * the presision of float and double values
     */
    protected static int precision = 2;
    
    /**
     * variable names and addresses
     */
    protected static HashMap< String, Value > values = new HashMap<>();
    
    /**
     * instruction of current execution
     */
    protected static List< String > currentInstructions = new ArrayList<>();
    
    /**
     * Labels
     */
    protected static ArrayList< LabelValue > labels = new ArrayList<>();
    
    /**
     * the address of first instruction and thirdToken instruction
     */
    protected static int firstInstructionAddress = 0;
    protected static int firstDataAddress  = 0;
    
    /**
     * IVM RAM size 
     */
    private static int SIZE;
    
    static
    {
        SIZE = (int)Runtime.getRuntime().freeMemory() / 8;
        
        // alocating MEMORY
        MEMORY = ByteBuffer.allocateDirect( SIZE );
        
        // initialize the MEMORY
        for ( int i = 0; i < SIZE; ++i )
            MEMORY.put( (byte)0 );
        
        // initialize INSTRUCTION_SIZE
        INSTRUCTION_SIZE = String.valueOf( MEMORY.capacity()/*.length*/ - 1 ).length();
        
        // initialize ADDRESS_SIZE
        ADDRESS_SIZE = SIZE - 1;
        
        try 
        {
            // initialize LOG_OUTPUT to write logs to imachinelog file
            LOG_OUTPUT = new PrintStream( Files.newOutputStream( Paths.get( "imachinelog" ),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING  ) );
            ERR_OUTPUT = new PrintStream( Files.newOutputStream( Paths.get( "imachineerr" ),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING ) );
        }  // end try
        catch (IOException ex) 
        {
            Logger.getLogger(IMachine.class.getName()).log(Level.SEVERE, null, ex);
            //LOGOUTPUT = null;
        } // end try...catch // end try...catch // end try...catch // end try...catch
    } // end static block initializer
    
    /**
     * 
     * @return 0 if not problem occured <br>
     * other number if an ERROR occured
     */
    public static int runNext()
    {
        fetch();
        decode();
        
        return execute();
    } // end method execute
    
    /**
     * 
     * @param instruction the instruction to be parsed
     * @return the instruction as integer <br>
     */
    protected static int parseInstruction( String instruction ) throws NumberFormatException
    {
        return Integer.parseInt( instruction );
    } // end method parseInstruction
    
    /**
     * 
     * @param instruction the instruction
     * @return the operation code of this instruction
     */
    protected static int getOperationCode( int instruction )
    {
        return instruction / (int) Math.pow( 10, INSTRUCTION_SIZE );
    } // end method getOperationCode
    
    /**
     * 
     * @param instruction the instruction
     * @return the operand of this instruction
     */
    protected static int getOperand( int instruction )
    {
        return instruction % (int) Math.pow( 10, INSTRUCTION_SIZE );
    } // end method getInstructionOperation
    /**
     * fetch next instruction from MEMORY
     */
    protected static void fetch()
    {
        //System.out.println( "instructionCounter = " + instructionCounter );
        instructionRegister = MEMORY.getInt( instructionCounter );
        //System.err.printf( "Fetch: %d -> %d%n", instructionCounter, instructionRegister );
        instructionCounter += 4;
    } // end method fetch
    
    /**
     * 
     */
    protected static void decode()
    {
        operationCode = getOperationCode( instructionRegister );
        //System.err.println( "operationCode is: " + operationCode );
        operand = getOperand( instructionRegister );
        //System.err.println( "operand is : " + operand );
    } // end method decode
    
    protected static int execute()
    {
        LOG_OUTPUT.printf("%s: %+d %0" + INSTRUCTION_SIZE + "d%n", "تنفيذ", operationCode, operand );
        switch ( operationCode )
        {       
            // READ cases
            case READ:
                switch ( calculationType )
                {
                    case BYTE:
                        MEMORY.put( operand, keyboard.nextByte() );
                        break;
                    case SHORT:
                        MEMORY.putShort( operand, keyboard.nextShort() );
                        break;
                    case CHAR:
                        MEMORY.putChar( operand, keyboard.nextLine().charAt( 0 ) );
                        break;
                    case INT:
                        MEMORY.putInt( operand, keyboard.nextInt() );
                        break;
                    case LONG:
                        MEMORY.putLong( operand, keyboard.nextLong() );
                        break;
                    case FLOAT:
                        MEMORY.putFloat( operand, keyboard.nextFloat() );
                        break;
                    case DOUBLE:
                        MEMORY.putDouble( operand, keyboard.nextDouble() );
                        break;
                    case CHARS:
                        asignstr = keyboard.nextLine();
                        if ( operand == 0 )
                        {
                            break;
                        } // end if
                        else
                        {
                            for ( int i = 0; ; i++ )
                            {
                                if ( MEMORY.getChar( operand ) == '\0' )
                                    break;
                                MEMORY.putChar( operand, asignstr.charAt( 0 ) );
                                operand += 2;
                            } // end for
                        } // end if...else
                        break;
                } // end switch
                break;    
                
            // WRITE cases    
            case WRITE:
                switch ( calculationType )
                {
                    case BYTE:
                        System.out.printf("%d", MEMORY.get( operand ) );
                        break;
                    case SHORT:
                        System.out.printf("%d", MEMORY.getShort( operand ) );
                        break;
                    case CHAR:
                        System.out.printf("%c", MEMORY.getChar( operand ) );
                        break;
                    case INT:
                        System.out.printf("%d", MEMORY.getInt( operand ) );
                        break;
                    case LONG:
                        System.out.printf("%d", MEMORY.getLong( operand ) );
                        break;
                    case FLOAT:
                        System.out.printf("%." + precision + "f", MEMORY.getFloat( operand ) );
                        break;
                    case DOUBLE:
                        System.out.printf("%." + precision + "f", MEMORY.getFloat( operand ) );
                        break;
                    case CHARS:
                        if ( operand == 0 )
                            System.out.print( asignstr );
                        else
                        {
                            char currentChar;
                            while ( (currentChar = MEMORY.getChar( operand ) ) != '\0' )                
                            {                   
                                operand += 2;                
                                System.out.printf("%c", currentChar );              
                            } // end while
                        } // end if...else
                        
                        break;
                } // end switch

                break;
                
            case WRITENL:
                System.out.printf( "%n" );
                break;
                    
            // LOAD cases    
            case LOAD:
                switch (calculationType )
                {
                    case BYTE:
                        accum = MEMORY.get( operand );
                        break;
                    case SHORT:
                        accum = MEMORY.getShort( operand );
                        break;
                    case CHAR:
                        accum = (int)MEMORY.getChar( operand );
                        break;
                    case INT:
                        accum = MEMORY.getInt( operand );
                        break;
                    case LONG:
                        accum = MEMORY.getLong( operand );
                        break;
                    case FLOAT:
                        accum = MEMORY.getFloat( operand );
                        break;
                    case DOUBLE:
                        accum = MEMORY.getDouble( operand );
                        break;
                    case CHARS:
                        char c;
                        asignstr = "";
                        while ( ( c = MEMORY.getChar( operand ) ) != '\0' )
                        {
                            asignstr += String.valueOf( c );
                            operand += 2;
                        } // end while
                            
                        break;
                } // end switch
                
            // STORE cases
            case STORE:
                switch (calculationType )
                {
                    case BYTE:
                        MEMORY.put( operand, (byte)accum );
                        break;
                    case SHORT:
                        MEMORY.putShort( operand, (short)accum );
                        break;
                    case CHAR:
                        MEMORY.putChar( operand, (char)accum );
                        break;
                    case INT:
                        MEMORY.putInt( operand, (int)accum );
                        break;
                    case LONG:
                        MEMORY.putLong( operand, (long)accum );
                        break;
                    case FLOAT:
                        MEMORY.putFloat( operand, (float)accum );
                        break;
                    case DOUBLE:
                        MEMORY.putDouble( operand, accum );
                        break;
                    case CHARS:
                        char c;
                        int i = 0;
                        while ( ( c = MEMORY.getChar( operand ) ) != '\0'
                                && i < asignstr.length() )
                        {
                            MEMORY.putChar( operand, asignstr.charAt( i++ ) );
                            operand += 2;
                        } // end while
                } // end switch
                break;
                
            // change calculation type   
            case CALTYPE:
                calculationType = operand;
                break;
                
            // calculations    
            case ADD:
                accum += getSecondOperand( calculationType, operand );
                break;
                
            case SUB:
                accum -= getSecondOperand( calculationType, operand );
                break;
                
            case DIV:
                try
                {
                    accum /= getSecondOperand( calculationType, operand );
                    break;
                }
                catch ( ArithmeticException e )
                {
                    System.err.println( "***محاولة القسمة على صفر***" );
                    return -1;
                }
            
            case MUL:
                accum *= getSecondOperand( calculationType, operand );
                break;
            
            case MOD:
                accum %= getSecondOperand( calculationType, operand );
                break;
                
            case EXP:
                accum = Math.pow( accum, operand /*getSecondOperand( calculationType, operand )*/ );
                break;
                
            case INC:
                switch ( calculationType )
                {
                    case BYTE:
                        MEMORY.put( operand, (byte) ( MEMORY.get( operand ) + 1 ) );
                        break;
                    case SHORT:
                        MEMORY.putShort( operand, (short) ( MEMORY.getShort( operand ) + 1 ) );
                        break;
                    case CHAR:
                        MEMORY.putChar( operand, (char)( MEMORY.getChar( operand ) + 1 ) );
                        break;
                    case INT:
                        MEMORY.putInt( operand, MEMORY.getInt( operand ) + 1 );
                        break;
                    case LONG:
                        MEMORY.putLong( operand, MEMORY.getLong( operand ) + 1 );
                        break;
                    case FLOAT:
                        MEMORY.putFloat( operand, MEMORY.getFloat( operand ) + 1 );
                        break;
                    case DOUBLE:
                        MEMORY.putDouble( operand, MEMORY.getDouble( operand ) + 1 );
                        break;
                    case CHARS:
                        System.err.println( "لا يمكن جمع نص مع الرقم 1" );
                } // end switch
                break;
                
                case DEC:
                switch ( calculationType )
                {
                    case BYTE:
                        MEMORY.put( operand, (byte) ( MEMORY.get( operand ) - 1 ) );
                        break;
                    case SHORT:
                        MEMORY.putShort( operand, (short) ( MEMORY.getShort( operand ) - 1 ) );
                        break;
                    case INT:
                        MEMORY.putInt( operand, MEMORY.getInt( operand ) - 1 );
                        break;
                    case LONG:
                        MEMORY.putLong( operand, MEMORY.getLong( operand ) - 1 );
                        break;
                    case FLOAT:
                        MEMORY.putFloat( operand, MEMORY.getFloat( operand ) - 1 );
                        break;
                    case DOUBLE:
                        MEMORY.putDouble( operand, MEMORY.getDouble( operand ) - 1 );
                        break;
                    case CHARS:
                        System.err.println( "لا يمكن طرح 1 من نص!" );
                } // end switch
                break;
                
            case PRC:
                if ( operand >= 1 && operand <= 15 )
                    precision = operand;
                break;
            
            case BRANCH:
                instructionCounter = operand;
                break;
                
            case BRANCHNEG:
                if ( accum < 0.0 )
                    instructionCounter = operand;
                break;
                
            case BRANCHZERO:
                if ( accum == 0.0 )
                    instructionCounter = operand;
                break;
                
            case HALT:
                System.out.println( "***انتهى التنفيذ***" );
                LOG_OUTPUT.printf("%s: %s%n", Calendar.getInstance().getTime(), "***انتهى التنفيذ***" );
                return END_OF_PROCESS;
        } // end switch
        
        return 0;
    } // end method execute
    
    public static void loadProgram( File file )
    {
        try
        {
            Scanner sc = new Scanner( file );
            while ( true )
            {
                try
                {
                    String instruction = formatIMachineInstruction( 
                        formatIMAinstruction( sc.nextLine().trim() ) );
                    if ( instruction.equals( "continue" ) )
                        continue;
                    
                    // end new enhancement
                    System.out.printf( "%0" + INSTRUCTION_SIZE + "d > %s\n", instructionCounter, instruction );
                    MEMORY.putInt( instructionCounter, parseInstruction( instruction.trim() ) );
                    /*if ( firstInstructionAddress == 0 )
                        firstInstructionAddress = instructionCounter;*/
                    instructionCounter += 4;
                    //MEMORY[ instructionCounter++ ] = parseInstruction( instruction.trim() );
                } // end try // end try
                catch ( Exception e )
                {
                    //e.printStackTrace();
                    instructionCounter = firstInstructionAddress;
                    sc.close();
                    break;
                } // end try...catch
            
            } // end while
            
        } // end try // end try
        catch ( FileNotFoundException ex )
        {
            throw new RuntimeException( "***خطــــأ -> الملف غير موجود***" );
        } // end try...catch
        
    } // end method loadProgram
        
    private static class LabelValue
    {
        public String label;
        public int address;

        public LabelValue(String lb, int addrs) {
            this.label = lb;
            this.address = addrs;
        } // end constructor

        @Override
        public String toString() {
            return "LabelValue{" + "label=" + label + ", address=" + address + '}';
        }
        
        
        
    } // end class labelValue
    public static void compile( Path path )
    {
        prepareValues( path );
        
        try {
                String outputFileName = path.toFile().getName();
                
                PrintStream imaobj = new PrintStream( Files.newOutputStream( Paths.get( 
                    outputFileName.substring( 0, outputFileName.lastIndexOf( "." ) ) + ".ivm" ), 
                    StandardOpenOption.CREATE,    
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING ) );
            
                values.keySet().stream().forEach( e ->
                {
                    Value value = values.get( e );
                    value.address = firstDataAddress;
                    firstDataAddress += value.size;
                    imaobj.printf("+%02d %s %d%n", value.type, value.data, value.address );
                });
                
                for ( int i = 0; i < currentInstructions.size(); ++i )
                {
                    String inst = currentInstructions.get( i );
                    StringTokenizer tokens = new StringTokenizer( inst, " ", false );
                    final int tokenCount = tokens.countTokens();
                    String strs[] = new String[ tokenCount ];
                    
                    for ( int ii = 0; ii < tokenCount; ++ii )
                    {
                        strs[ ii ] = tokens.nextToken();
                    } // end for
                    
                    if ( strs[ 0 ].trim().equals( LABEL_IMA.trim() ) )
                    {   
                        labels.add( new LabelValue( strs[ 1 ], i * 4 ) );
                    } // end if
                } // end for
                
                //labels.forEach( System.out::println );
                
                currentInstructions.stream()
                .map( e -> // format IMA instructions
                {
                    StringTokenizer tokens = new StringTokenizer( e, " ", false );
                    final int tokenCount = tokens.countTokens();
                    String strs[] = new String[ tokenCount ];
                    for ( int i = 0; i < tokenCount; ++i )
                    {
                        strs[ i ] = tokens.nextToken();
                    } // end for
                    
                    strs[ 0 ] = formatIMAinstruction( strs[ 0 ].trim() );
                    if ( strs.length > 1 )
                    {
                        strs[ 1 ] = formatIMAinstruction( strs[ 1 ] );
                        Value value = values.get( strs[ 1 ].trim() );
                        if ( value != null )
                            strs[ 1 ] = String.valueOf( value.address );
                    } // end if
                    else
                    {
                        return String.join( " ", strs ).concat( " 00" );
                    } // end if...else
                        
                    return String.join( " ", strs );
                })
                .map( e ->
                {
                    StringTokenizer tokens = new StringTokenizer( e, " ", false );
                    final int tokenCount = tokens.countTokens();
                    String strs[] = new String[ tokenCount ];
                    for ( int i = 0; i < tokenCount; ++i )
                    {
                        strs[ i ] = tokens.nextToken();
                    } // end for
                    
                    String str1 = null;
                    for ( LabelValue lb : labels)
                        if ( strs.length > 1 && strs[ 1 ].trim().equals( lb.label.trim() ) )
                        {
                            str1 = String.valueOf( lb.address );
                        } // end if
                    if ( str1 != null )
                        strs[ 1 ] = str1;
                    return String.join( " ", strs );
                })
                .forEach( imaobj::println );
                
                imaobj.flush();
        } 
        catch (IOException ex)
        {
            Logger.getLogger(IMachine.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        values.clear();
        currentInstructions.clear();
        labels.clear();
    } // end method compile
    
    private static void prepareValues( Path path )
    {
        int instructions = 0;
        
        try 
        {
            Scanner sc = new Scanner( path.toFile() );
            
            while ( true )
            {
                String instruction = sc.nextLine().trim();
                if ( instruction.isEmpty() )
                    continue;
     
                String delemeter;
                if ( instruction.contains( "\"" ) )
                    delemeter = "\"";
                else
                    delemeter = " ";
                StringTokenizer tokens = new StringTokenizer( instruction, delemeter, false );
                String split[] = new String[ tokens.countTokens() ];
                int a = 0;
                while ( tokens.hasMoreTokens() )
                {
                    if ( a < 2 )
                        split[ a++ ] = formatIMAinstruction( tokens.nextToken() );
                    else
                        split[ a++ ] = tokens.nextToken();
                } // end while
                
                // get the name, value and type of declarations
                prepareIMachineValues( split );
                // count the number of instructions
                if ( isInstruction( split[ 0 ].trim() ) )
                {
                    ++instructions;
                    currentInstructions.add( instruction );
                } // end if
                
            } // end while
            
        } // end try
        catch ( NoSuchElementException nse )
        {
            firstDataAddress = instructions * 4;
        } // end catch
        catch (FileNotFoundException ex) 
        {
            String error = String.format( "%s: %s %s, %s%n", Calendar.getInstance().getTime(),
                "إستثناء", "خلال تهيئة المتغيرات", "ملف البرنامج المحدد غير موجود");
            LOG_OUTPUT.printf( "%s%n", error );
            ERR_OUTPUT.printf( "%s%n", error );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    } // end method prepareValues
    
    private static class Value
    {
        public final int type;
        public final int size;
        public final Object data;
        
        public int address = 0;

        public Value(int type, int size, Object otherData) 
        {
            this.type = type;
            this.size = size;
            this.data = otherData;
        } // end tree-argument Value constructor

        @Override
        public String toString()
        {
            return "Value{" + "type=" + type + ", size=" + size + 
                   ", data=" + data + ", address=" + address + '}';
        } // end method toString
        
    } // end inner private class Value
    
    private static void prepareIMachineValues( String split[] )
    {   
        //if ( split.length < 3 )
            //return;
        
        if ( split[ 0 ].trim().startsWith( "//" ) || split[ 0 ].trim().startsWith( "#" ) )
            return;
        
        System.err.println( Arrays.toString(split));
        
        int token1 = 0;
        int token2 = 1;
        int token3 = 2;
        
        switch ( Integer.parseInt( split[ token1 ].trim() ) )
        {
            case BYTE:
                values.put(split[ token3 ].trim(), new Value( 
                    BYTE, 
                    Byte.BYTES, 
                    Byte.parseByte( split[ token2 ].trim() ) ) );
                return;
            case SHORT:
                values.put(split[ token3 ], new Value( 
                    SHORT, 
                    Short.BYTES, 
                    Short.parseShort( split[ token2 ].trim() ) ) );
                return;
            case CHAR:
                values.put(split[ token3 ].trim(), new Value( 
                    CHAR, 
                    Character.BYTES, 
                    split[ token2 ].trim() ) );
                return;
            case INT:
                values.put(split[ token3 ].trim(), new Value( 
                    INT, 
                    Integer.BYTES, 
                    Integer.parseInt( split[ token2 ].trim() ) ) );
                return;
            case LONG:
                values.put(split[ token3 ].trim(), new Value( 
                    LONG, 
                    Long.BYTES, 
                    Long.parseLong( split[ token2 ].trim() ) ) );
                return;
            case FLOAT:
                values.put(split[ token3 ].trim(), new Value( 
                    FLOAT, 
                    Float.BYTES, 
                    Float.parseFloat( split[ token2 ].trim() ) ) );
                return;
            case DOUBLE:
                values.put(split[ token3 ].trim(), new Value( 
                    DOUBLE, 
                    Double.BYTES, 
                    Double.parseDouble( split[ token2 ].trim() ) ) );
                return;
            case CHARS:
                values.put(split[ token3 ].trim(), new Value( 
                    CHARS, 
                    ( split[ token2 ].length() + 1 ) * 2, 
                    "\"" + split[ token2 ] + "\"" ) );
                return;
                
                case LABEL:
                    labels.add( new LabelValue( split[ 1 ].trim(), 0 ) );
              
        } // end switch
                  
    } // end method prepareIMachineValues
    
    private static boolean isInstruction( String instruction )
    {   
        if ( instruction.trim().startsWith( "//" ) ||
             instruction.trim().startsWith( "#" ) )
            return false;
        
        switch ( Integer.parseInt( instruction ) )
        {
            case BYTE:
                return false;
            case SHORT:
                return false;
            case CHAR:
                return false;
            case INT:
                return false;
            case LONG:
                return false;
            case FLOAT:
                return false;
            case DOUBLE:
                return false;
            case CHARS:
                 return false;
            default:
                return true;
        } // end switch
        
    } // end method isInstruction
    
    private static String formatIMAinstruction( String IMA_Instruction )
    {      
        
        // ------------------------------READ-----------------------------------
        if ( IMA_Instruction.startsWith( READ_IMA ) )
        {
            return IMA_Instruction.replaceAll( READ_IMA, String.valueOf( READ ) );
        } // end if
        
        // ------------------------------WRITE-----------------------------------
        if ( IMA_Instruction.startsWith( WRITENL_IMA ) )
        {
            return IMA_Instruction.replaceAll( WRITENL_IMA, String.valueOf( WRITENL ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( WRITE_IMA ) )
        {
            return IMA_Instruction.replaceAll( WRITE_IMA, String.valueOf( WRITE ) );
        } // end if
        
        // ------------------------------LOAD-----------------------------------
        if ( IMA_Instruction.startsWith( LOAD_IMA ) )
        {
            return IMA_Instruction.replaceAll( LOAD_IMA, String.valueOf( LOAD ) );
        } // end if
  
        // ------------------------------STORE-----------------------------------
        if ( IMA_Instruction.startsWith( STORE_IMA ) )
        {
            return IMA_Instruction.replaceAll( STORE_IMA, String.valueOf( STORE ) );
        } // end if
          
        //---------------------------ARITHMETIC---------------------------------
        if ( IMA_Instruction.startsWith( ADD_IMA ) )
        {
            return IMA_Instruction.replaceAll( ADD_IMA, String.valueOf( ADD ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( SUB_IMA ) )
        {
            return IMA_Instruction.replaceAll( SUB_IMA, String.valueOf( SUB ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( DIV_IMA ) )
        {
            return IMA_Instruction.replaceAll( DIV_IMA, String.valueOf( DIV ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( MUL_IMA ) )
        {
            return IMA_Instruction.replaceAll( MUL_IMA, String.valueOf( MUL ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( MOD_IMA ) )
        {
            return IMA_Instruction.replaceAll( MOD_IMA, String.valueOf( MOD ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( EXP_IMA ) )
        {
            return IMA_Instruction.replaceAll( EXP_IMA, String.valueOf( EXP ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( INC_IMA ) )
        {
            return IMA_Instruction.replaceAll( INC_IMA, String.valueOf( INC ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( DEC_IMA ) )
        {
            return IMA_Instruction.replaceAll( DEC_IMA, String.valueOf( DEC ) );
        } // end if
        
        //---------------------------BRANCH-------------------------------------
        
        if ( IMA_Instruction.startsWith( BRANCHNEG_IMA ) )
        {
            return IMA_Instruction.replaceAll( BRANCHNEG_IMA, String.valueOf( BRANCHNEG ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( BRANCHZERO_IMA ) )
        {
            return IMA_Instruction.replaceAll( BRANCHZERO_IMA, String.valueOf( BRANCHZERO ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( BRANCH_IMA ) )
        {
            return IMA_Instruction.replaceAll( BRANCH_IMA, String.valueOf( BRANCH ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( HALT_IMA ) )
        {
            return IMA_Instruction.replaceAll( HALT_IMA, String.valueOf( HALT ) );
        } // end if
        
        //---------------------------OTHER---------------------------------------
        if ( IMA_Instruction.startsWith( CALTYPE_IMA ) )
        {
            return IMA_Instruction.replaceAll( CALTYPE_IMA, String.valueOf( CALTYPE ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( PRC_IMA ) )
        {
            return IMA_Instruction.replaceAll( PRC_IMA, String.valueOf( PRC ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( SQRT_IMA ) )
        {
            return IMA_Instruction.replaceAll( SQRT_IMA, String.valueOf( SQRT ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( LABEL_IMA ) )
        {
            return IMA_Instruction.replaceAll( LABEL_IMA, String.valueOf( LABEL ) );
        } // end if
        
        // ------------------------------DECLARATION----------------------------
        if ( IMA_Instruction.startsWith( BYTE_IMA ) )
        {
            return IMA_Instruction.replaceAll( BYTE_IMA, String.valueOf( BYTE ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( SHORT_IMA ) )
        {
            return IMA_Instruction.replaceAll( SHORT_IMA, String.valueOf( SHORT ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( INT_IMA ) )
        {
            return IMA_Instruction.replaceAll( INT_IMA, String.valueOf( INT ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( LONG_IMA ) )
        {
            return IMA_Instruction.replaceAll( LONG_IMA, String.valueOf( LONG ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( FLOAT_IMA ) )
        {
            return IMA_Instruction.replaceAll( FLOAT_IMA, String.valueOf( FLOAT ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( DOUBLE_IMA ) )
        {
            return IMA_Instruction.replaceAll( DOUBLE_IMA, String.valueOf( DOUBLE ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( CHARS_IMA ) )
        {
            return IMA_Instruction.replaceAll( CHARS_IMA, String.valueOf( CHARS ) );
        } // end if
        
        if ( IMA_Instruction.startsWith( CHAR_IMA ) )
        {
            return IMA_Instruction.replaceAll( CHAR_IMA, String.valueOf( CHAR ) );
        } // end if
            
        return IMA_Instruction;
    } // end method formatISMInstruction
    
    private static String formatIMachineInstruction( String instruction )
    {
        if ( instruction.trim().startsWith( "//" ) )
            return "continue";
        
        String builder = instruction;
                    
        if ( builder.contains( " " ) )
        {   
            String delemeter;
            if ( instruction.contains( "\"" ) )
                delemeter = "\"";
            else
                delemeter = " ";
            // split string into tokens
            StringTokenizer tokens = new StringTokenizer( builder, delemeter, false );
            String split[] = new String[ tokens.countTokens() ];
            int a = 0;
            while ( tokens.hasMoreTokens() )
            {
                split[ a++ ] = tokens.nextToken().trim();
            } // end while
            
            //System.err.println( Arrays.toString(split));
            
            int secondToken = 2 - 1;
            
            switch ( Integer.parseInt( split[ 0 ] ) )
            {
                case BYTE:
                    MEMORY.put( Integer.parseInt( split[ 2 ] ), Byte.parseByte(split[ secondToken ] ) );
                    //++firstDataAddress;
                    LOG_OUTPUT.printf("%s: %s%n", "تنفيذ", instruction );
                    return "continue";
                case SHORT:
                    MEMORY.putShort( Integer.parseInt( split[ 2 ] ), Short.parseShort(split[ secondToken ] ) );
                    //firstDataAddress += 2;
                    LOG_OUTPUT.printf("%s: %s%n", "تنفيذ", instruction );
                    return "continue";
                case CHAR:
                    MEMORY.putChar( Integer.parseInt( split[ 2 ] ), instruction.charAt( 
                        instruction.lastIndexOf( "'" ) - 1 ) );
                    //instructionCounter += 2;
                    LOG_OUTPUT.printf("%s: %s%n", "تنفيذ", instruction );
                    return "continue";
                case INT:
                    MEMORY.putInt(Integer.parseInt( split[ 2 ] ), Integer.parseInt(split[ secondToken ] ) );
                    //instructionCounter += 4;
                    LOG_OUTPUT.printf("%s: %s%n", "تنفيذ", instruction );
                    return "continue";
                case LONG:
                    MEMORY.putLong(Integer.parseInt( split[ 2 ] ), Long.parseLong(split[ secondToken ] ) );
                    //instructionCounter += 8;
                    return "continue";
                case FLOAT:
                    MEMORY.putFloat(Integer.parseInt( split[ 2 ] ), Float.parseFloat(split[ secondToken ] ) );
                    //instructionCounter += 4;
                    LOG_OUTPUT.printf("%s: %s%n", "تنفيذ", instruction );
                    return "continue";
                case DOUBLE:
                    MEMORY.putDouble(Integer.parseInt( split[ 2 ] ), Double.parseDouble(split[ secondToken ] ) );
                    //instructionCounter += 8;
                    LOG_OUTPUT.printf("%s: %s%n", "تنفيذ", instruction );
                    return "continue";
                case CHARS:
                    // asing the current String to asignstr
                    int currentPosition = Integer.parseInt( split[ 2 ] );
                    asignstr = instruction.substring( instruction.indexOf( "\"" ) + 1, 
                        instruction.lastIndexOf("\"") );
                    // loop the asignstr characters
                    // and put each character on memory
                    // then increment instructionCounter by 2
                    for ( int i = 0; i < asignstr.length(); ++i )
                    {
                        MEMORY.putChar( currentPosition, asignstr.charAt( i ) );
                        currentPosition += 2;
                    } // end for
                    // destroy the content of asignstr
                    asignstr = null;
                    // put end of string character
                    MEMORY.putChar( currentPosition, '\0' );
                    // increment instructionCounter by 2
                    //instructionCounter += 2;
                    LOG_OUTPUT.printf("%s: %s%n", "تنفيذ", instruction );
                    return "continue";
                
            } // end switch
                  
            // convert the thirdToken token to an integer
            int op = Integer.parseInt(split[ split.length - 1 ] );
                        
            // format the integer to the INSTRUCTION_SIZE length 
            // then asign it to thirdToken token
            split[ split.length - 1 ] = String.format( "%0" + INSTRUCTION_SIZE + "d", op );
                        
            // join all token in one string and delete all spaces
            builder = String.join( "", split ).replace(' ', '\0' );
                        
            // asign it to instruction
            return builder;
        } // end if
        else
            return instruction;
    } // end method formatIMachineInstruction
    
    private static double getSecondOperand( int caltype, int operand )
    {
        switch ( caltype )
        {
            case BYTE:
                return MEMORY.get( operand );
            case SHORT:
                return MEMORY.getShort( operand );
            case CHAR:
                return MEMORY.getChar( operand );
            case INT:
                return MEMORY.getInt( operand );
            case LONG:               
                return MEMORY.getLong( operand );
            case FLOAT:
                return MEMORY.getFloat( operand );
            case DOUBLE:               
                return MEMORY.getDouble( operand );
            default:
                throw new IllegalArgumentException( "لم يحدد نوع عملية صحيح" );
        } // end switch
    } // end method getSecondOperand
    
    public static void welcomeToIMachine()
    {
        LOG_OUTPUT.printf("%s: %s%n", Calendar.getInstance().getTime(), "***بداية تنفيذ الآلة الإفتراضية***" );
        System.out.printf("%s: %s%n", Calendar.getInstance().getTime(), "***بداية تنفيذ الآلة الإفتراضية***" );
        System.out.println( "السلام عليكم" );
        System.out.println( "***مرحبا بكم في آلة إلياس الإفتراضية***" );
        System.out.printf( "حجم الذاكرة الرئيسية: %d بايت%n", SIZE );
        System.out.printf( "حجم التعليمة: %d%n", INSTRUCTION_SIZE );
        //System.out.printf( "آخر عنوان للذاكرة الرئيسية: %d%n", ADDRESS_SIZE );
    } // end method welocomeToBibrass
    
    public static void memoryDump()
    {
        System.out.println( "***افراغ الذاكرة***" );
        
        int tab = SIZE / 100 + 3;
        
        System.out.printf( "%" + ( INSTRUCTION_SIZE + tab ) + "s", "" );
        for ( int i = 0; i < 10; ++i )
        {
            System.out.printf( "%-" + ( INSTRUCTION_SIZE + tab ) + "d", i );
        } // end for
        
        System.out.println();
        System.out.println();
        //System.out.printf( "%n%" + INSTRUCTION_SIZE + "d", 0 );

        for ( int i = 0; i < SIZE; ++i )
        {
            System.out.printf( "%-" + ( INSTRUCTION_SIZE + tab ) + "d", Byte.toUnsignedInt( MEMORY.get( i ) ) );
            if ( i % 10 == 0 && i != 0 )
            {
                System.out.println();
                System.out.printf( "%-" + ( INSTRUCTION_SIZE + tab ) + "d", i );
            } // end if
                
        } // end for
        
        System.out.println();
    } // end method memoryDump
    
    private static void help()
    {
        System.out.println( "السلام عليكم و مرحبا بكم في الآلة الافتراضية" );
        System.out.printf( "1. %s, %s, %s, %s, %s, %s: %s%n", "ص", "ت", "ترجمة", "تصريف", "c", "compile",
            "لترجمة برنامج إلى لغة الآلة الإفتراضية");
        System.out.printf( "2. %s, %s, %s, %s: %s%n", "ح", "تحميل", "l", "load",
            "لتحميل ملف برنامج مترجم إلى ذاكرة الآلة الافتراضية" );
        System.out.printf( "3. %s, %s, %s, %s: %s%n", "ن", "تنفيذ", "r", "run",
            "لبدأ تنفيذ البرنامج المحمل" );
        System.out.printf( "4. %s, %s, %s, %s: %s%n", "م", "مساعدة", "h", "help",
            "لعرض المساعدة" );
        System.out.printf( "5. %s, %s, %s, %s: %s%n", "ف", "إفراغ", "d", "dump",
            "لإفراغ محتوى الذاكرة إلى الشاشة" );
        System.out.printf( "6. %s, %s, %s, %s: %s%n", "تص", "تصفير", "rs", "reset",
            "لتصفير ذواكر الآلة الافتراضية" );
        System.out.printf( "7. %s, %s, %s, %s: %s%n", "خ", "خروج", "q", "quit",
            "لإنهاء جلسة الآلة الافتراضية" );
    } // end method help
    
    private static void compile()
    {
        System.out.print( " " + "أدخل اسم ملف البرنامج الذي تريد ترجمته: " );
        compile( Paths.get( keyboard.nextLine().trim() ) );
        //System.out.println( "تمت الترجمة بنجاح ولله الحمد" );
    } // end method compile
    
    private static void load()
    {
        System.out.print( " " + "أدخل اسم ملف البرنامج الذي تريد تحميله:" + " " );
        loadProgram( Paths.get( keyboard.nextLine().trim() ).toFile() );
    } // end method load
    
    private static void run()
    {
        int running = 0;
        while ( running != END_OF_PROCESS )
            running = runNext();
        instructionCounter = 0;
    } // end method run
    
    private static void reset()
    {
        accum = 0;
        for ( int i = 0; i < MEMORY.capacity(); ++i )
            MEMORY.put( i, (byte)0 );
        operationCode = 0;
        operand = 0;
        instructionRegister = 0;
        instructionCounter = 0;
        asignstr = null;
        precision = 2;
        values.clear();
        currentInstructions.clear();
        labels.clear();
        firstInstructionAddress = 0;
        firstDataAddress  = 0;   
    } // end method destroyCurrentProcess
    
    public static void main( String[] args ) throws FileNotFoundException, InterruptedException
    {
        if ( args.length > 0 )
        {
            String fileName = args[ 0 ];
            if ( !fileName.endsWith( ".ima" ) )
            {
                System.err.println( "Error: file must be end with .ima" );
                return;
            } // end if
            else if ( !Files.exists( Paths.get( fileName ) ) )
            {
                System.err.println( "Error: file '" + fileName + "' doesn't exists!" );
                return;
            } // end else...if
            
            compile( Paths.get( fileName ) );
            fileName = fileName.substring( 0, fileName.lastIndexOf( "." ) ) + ".ivm";
            loadProgram( new File( fileName ) );
            run();
            
            reset();
            return;
        } // end if
        /*boolean b = true;
        
        String instructions[] = new String[] {
            "read8 45 bytev", "read16 45 shortv", "readc16 45 charv", 
            "read32 45 intv", "read64 45 longv", "readf32 45 floatv", 
            "readf64 45 doublev", "readchrs 45 charsv",  };
        for ( String str : instructions )
            System.out.printf( "%s => %s%n", str, formatIMAinstruction( str ) );
        
        if ( b )
            System.exit( 0 );//*/
        
        welcomeToIMachine();
        
        WHILE:
        while ( true )
        {
            System.out.print( " ؟ " );
            String input = keyboard.nextLine();
            switch ( input )
            {
                case "h": case "م": case "help": case "مساعدة":
                    help();
                    break;
                case "c": case "ص": case "ت": case "compile": case "ترجمة":
                case "تصريف":
                    try 
                    {
                        compile();
                    } 
                    catch (Exception e) 
                    {
                        e.printStackTrace();
                        System.err.println( "فشلت عملية الترجمة" );
                    }
                    break;
                case "l": case "ح": case "load": case "تحميل":
                    try 
                    {
                        load();
                    } 
                    catch (Exception e) 
                    {
                        //System.err.println( "حدث خطأ أثناء تحميل البرنامج" );
                    }
                    break;
                case "r": case "ن": case "run": case "تنفيذ":
                    try 
                    {
                        run();
                        break;
                    } 
                    catch (Exception e) 
                    {
                        e.printStackTrace();
                        e.printStackTrace( LOG_OUTPUT );
                        System.err.println( "حدث خطأ أثناء تنفيذ البرنامج" );
                        break;
                    }
                case "q": case "خ": case "quit": case "خروج":
                    System.out.print( "أتريد الخروج حقا؟ ن/لا " );
                    String answer = keyboard.nextLine();
                    if ( answer.startsWith( "ن" ) || answer.startsWith( "y" ) )
                        break WHILE;    
                    break;
                case "d": case "ف": case "dump": case "إفراغ":
                    memoryDump();
                    break;
                case "rs": case "تص": case "reset": case "تصفير":
                    reset();
                    break;
                default:
                    System.out.println( "أكتب مساعدة أو help أو م أو h من أجل المساعدة" );
            } // end switch
        } // end while
        
        System.exit( 0 );
    } // end main
    
} // end class IMachine
