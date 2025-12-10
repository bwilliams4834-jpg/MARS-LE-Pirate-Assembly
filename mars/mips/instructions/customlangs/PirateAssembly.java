    package mars.mips.instructions.customlangs;
    import mars.simulator.*;
    import mars.mips.hardware.*;
    import mars.mips.instructions.syscalls.*;
    import mars.*;
    import mars.util.*;
    import java.util.*;
    import java.io.*;
    import mars.mips.instructions.*;
    import java.util.Random;


public class PirateAssembly extends CustomAssembly{
   
   
   @Override
    public String getName(){
        return "Pirate Assembly";
    }

    @Override
    public String getDescription(){
        return "Become a pirate and sail the Seven Seas";
    }

    @Override 
    protected void populate() {
        instructionList.add(
                new BasicInstruction("pool $t1,$t2,$t3",
            	 "Addition with overflow : set $t1 to ($t2 plus $t3)",
                BasicInstructionFormat.R_FORMAT,
                "000000 fffff sssss ttttt 00000 001000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int add1 = RegisterFile.getValue(operands[1]);
                     int add2 = RegisterFile.getValue(operands[2]);
                     int sum = add1 + add2;
                  // overflow on A+B detected when A and B have same sign and A+B has other sign.
                     if ((add1 >= 0 && add2 >= 0 && sum < 0)
                        || (add1 < 0 && add2 < 0 && sum >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
                     RegisterFile.updateRegister(operands[0], sum);
                  }
               }));

        instructionList.add(
                new BasicInstruction("loot $t1,$t2,-100",
            	 "Addition immediate with overflow : set $t1 to ($t2 plus signed 16-bit immediate)",
                BasicInstructionFormat.I_FORMAT,
                "000100 sssss fffff tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int add1 = RegisterFile.getValue(operands[1]);
                     int add2 = operands[2] << 16 >> 16;
                     int sum = add1 + add2;
                  // overflow on A+B detected when A and B have same sign and A+B has other sign.
                     if ((add1 >= 0 && add2 >= 0 && sum < 0)
                        || (add1 < 0 && add2 < 0 && sum >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
                     RegisterFile.updateRegister(operands[0], sum);
                  }
               }));

        instructionList.add(
                new BasicInstruction("sail target", 
            	 "Sail unconditionally : sail to statement at target address",
            	 BasicInstructionFormat.J_FORMAT,
                "000010 ffffffffffffffffffffffffff",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     RegisterFile.updateRegister("$t1", operands[0]);
                     Globals.instructionSet.processJump(
                        ((RegisterFile.getProgramCounter() & 0xF0000000)
                                | (operands[0] << 2)));            
                  }
               }));
        instructionList.add(
                new BasicInstruction("dig $t1,-100($t2)",
            	 "Load word : Set $t1 to contents of effective memory word address",
                BasicInstructionFormat.I_FORMAT,
                "000111 ttttt fffff ssssssssssssssss",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     try
                     {
                        RegisterFile.updateRegister(operands[0],
                            Globals.memory.getWord(
                            RegisterFile.getValue(operands[2]) + operands[1]));
                     } 
                         catch (AddressErrorException e)
                        {
                           throw new ProcessingException(statement, e);
                        }
                  }
               }));
        instructionList.add(
                new BasicInstruction("weigh $t1,$t2,$t3",
                "Set less than : If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     RegisterFile.updateRegister(operands[0],
                        (RegisterFile.getValue(operands[1])
                        < RegisterFile.getValue(operands[2]))
                                ? 1
                                : 0);
                  }
               }));
        instructionList.add(
                new BasicInstruction("sq target",
                "Side Quest : Set $ra to Program Counter (return address) then sail to statement at target address",
            	 BasicInstructionFormat.J_FORMAT,
                "010011 ffffffffffffffffffffffffff",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     Globals.instructionSet.processReturnAddress(31);// RegisterFile.updateRegister(31, RegisterFile.getProgramCounter());
                     Globals.instructionSet.processJump(
                        (RegisterFile.getProgramCounter() & 0xF0000000)
                                | (operands[0] << 2));
                  }
               }));
        instructionList.add(
                new BasicInstruction("dock $t1", 
            	 "Dock: Sail to statement whose address is in $t1",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 fffff 00000 00000 00000 001000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     Globals.instructionSet.processJump(RegisterFile.getValue(operands[0]));
                  }
               }));
        instructionList.add(
                new BasicInstruction("bury $t1,-100($t2)",
                "Bury : Store contents of $t1 into the ground (effective memory word address)",
            	 BasicInstructionFormat.I_FORMAT,
                "000101 ttttt fffff ssssssssssssssss",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     try
                     {
                        Globals.memory.setWord(
                            RegisterFile.getValue(operands[2]) + operands[1],
                            RegisterFile.getValue(operands[0]));
                     } 
                         catch (AddressErrorException e)
                        {
                           throw new ProcessingException(statement, e);
                        }
                  }
               }));
        instructionList.add(
                new BasicInstruction("abandon $t1,$t2,label",
                "Abandon Ship : Branch to statement at label's address if $t1 and $t2 are equal",
            	 BasicInstructionFormat.I_BRANCH_FORMAT,
                "111000 fffff sssss tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                  
                     if (RegisterFile.getValue(operands[0])
                        == RegisterFile.getValue(operands[1]))
                     {
                        Globals.instructionSet.processBranch(operands[2]);
                     }
                  }
               }));
        instructionList.add(
                new BasicInstruction("com $t1,$t2,label",
                "Compare : Branch to statement at label's address if $t1 and $t2 are not equal",
            	 BasicInstructionFormat.I_BRANCH_FORMAT,
                "111001 fffff sssss tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     if (RegisterFile.getValue(operands[0])
                        != RegisterFile.getValue(operands[1]))
                     {
                        Globals.instructionSet.processBranch(operands[2]);
                     }
                  }
               }));
        
         instructionList.add(
                new BasicInstruction("lc $t1,$t2",
            	 "Load Cannon: set $t1 to $t2, can be used to load balls into your cannon",
                BasicInstructionFormat.R_FORMAT,
                "000000 fffff sssss 00000 00000 001001",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int add1 = RegisterFile.getValue(operands[1]);
                     int sum = add1;
                  // overflow on A+B detected when A and B have same sign and A+B has other sign.
                     RegisterFile.updateRegister(operands[0], sum);
                  }
               }));

         instructionList.add(
                new BasicInstruction("fire $t1,$t2",
            	 "Fire Cannon: fire at $t1 with cannon $t2, decrements $t1 by 10 then sets $t2 to zero",
                BasicInstructionFormat.R_FORMAT,
                "000000 fffff sssss 00000 00000 110001",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int health = RegisterFile.getValue(operands[0]);
                     
                     // checks if $t2 has a value of 1 (loaded cannon)
                     if (RegisterFile.getValue(operands[1]) == 1) {
                        health -= 10;
                     }
                  // overflow on A+B detected when A and B have same sign and A+B has other sign.
                     RegisterFile.updateRegister(operands[0], health);
                  }
               }));

         instructionList.add(
                new BasicInstruction("ahoy label",
            	 "Ahoy : Lets you shout a message to the console from a string stored at the label",
                BasicInstructionFormat.I_BRANCH_FORMAT,
                "101010 ffffffffffffffffffffffffff",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {        
                     char ch = 0;
                     // Get the name of the label from the token list
                     String label = statement.getOriginalTokenList().get(1).getValue();
                     // Look up the label in the program symbol table to get its address
                     int byteAddress = Globals.program.getLocalSymbolTable().getAddressLocalOrGlobal(label);

                     try
                        {
                           ch = (char) Globals.memory.getByte(byteAddress);
                                             // won't stop until NULL byte reached!
                           while (ch != 0)
                           {
                              SystemIO.printString(new Character(ch).toString());
                              byteAddress++;
                              ch = (char) Globals.memory.getByte(byteAddress);
                           }
                        } 
                           catch (AddressErrorException e)
                           {
                              throw new ProcessingException(statement, e);
                           }
                     
                  }
                           
               }));

         instructionList.add(
                new BasicInstruction("split $t1,$t2",
            	 "Split Register Values : set $t1 and $t2 to the values added up and divided by 2. ",
                BasicInstructionFormat.R_FORMAT,
                "000000 fffff sssss 00000 00000 110010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int add1 = RegisterFile.getValue(operands[0]);
                     int add2 = RegisterFile.getValue(operands[1]);
                     
                     //get the sum and then average
                     int sum = add1 + add2;
                     int avg = (sum)/2;
                  // overflow on A+B detected when A and B have same sign and A+B has other sign.
                     if ((add1 >= 0 && add2 >= 0 && sum < 0)
                        || (add1 < 0 && add2 < 0 && sum >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
                     //set both registers with that average value to split.
                     RegisterFile.updateRegister(operands[0], avg);
                     RegisterFile.updateRegister(operands[1], avg);
                  }
               }));
         instructionList.add(
                new BasicInstruction("pil $t1,$t2",
            	 "Pillage: set $t1 to ($t1 + $t2), then set $t2 to zero",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss 00000 fffff 00000 110011",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int add1 = RegisterFile.getValue(operands[0]);
                     int add2 = RegisterFile.getValue(operands[1]);
                     int sum = add1 + add2;
                     int zero = 0;
                  
                     RegisterFile.updateRegister(operands[0], sum);
                     RegisterFile.updateRegister(operands[1], zero);
                  }
               }));
         instructionList.add(
                new BasicInstruction("wtp $t1",
            	 "Walk The Plank: A random number is generated 1 - 10, if its 10, $t1 is set to zero",
                BasicInstructionFormat.R_FORMAT,
                "000000 fffff 00000 00000 00000 110100",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     Random random = new Random();
                     int randomNum = random.nextInt(10) + 1;

                     int[] operands = statement.getOperands();
                     int zero = 0;
                     
                     if (randomNum == 10) {
                        SystemIO.printString("You took one step too far!\n");
                        RegisterFile.updateRegister(operands[0], zero);
                     }
                     else {
                        SystemIO.printString("You take a step forward on the plank.\n");
                     }
                  }
               }));
         instructionList.add(
                new BasicInstruction("duel $t1,$t2",
            	 "Pirate Duel: A random number is generated: 1 or 2. If its 1, $t1 wins the duel. If it's 2, $t2 wins. The winner takes all",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss fffff 00000 00000 110100",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     Random random = new Random();
                     int randomNum = random.nextInt(2) + 1;

                     int[] operands = statement.getOperands();
                     int zero = 0;
                     int add1 = RegisterFile.getValue(operands[0]);
                     int add2 = RegisterFile.getValue(operands[1]);
                     int sum = add1 + add2;
                     
                     
                     if (randomNum == 1) {
                        SystemIO.printString("Pirate 1 wins the Duel!\n");
                        RegisterFile.updateRegister(operands[0], sum);
                        RegisterFile.updateRegister(operands[1], zero);
                     }
                     else {
                        SystemIO.printString("Pirate 2 wins the Duel!\n");
                        RegisterFile.updateRegister(operands[1], sum);
                        RegisterFile.updateRegister(operands[0], zero);
                     }
                  }
               }));
         instructionList.add(
                new BasicInstruction("rep $t1,-100",
            	 "Repair: Addition immediate with overflow : set $t1 to ($t1 plus signed 16-bit immediate)",
                BasicInstructionFormat.I_FORMAT,
                "110110 sssss fffff tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int add1 = RegisterFile.getValue(operands[0]);
                     int add2 = operands[1] << 16 >> 16;
                     int sum = add1 + add2;
                  // overflow on A+B detected when A and B have same sign and A+B has other sign.
                     if ((add1 >= 0 && add2 >= 0 && sum < 0)
                        || (add1 < 0 && add2 < 0 && sum >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
                     RegisterFile.updateRegister(operands[0], sum);
                  }
               }));

         instructionList.add(
                new BasicInstruction("sab $t1",
            	 "Sabotage: lets you sabotage a register by setting $t1 to -1",
                BasicInstructionFormat.R_FORMAT,
                "000000 fffff 00000 00000 00000 110111",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int sabo = -1;
                     RegisterFile.updateRegister(operands[0], sabo);
                     
                  }
               }));
         instructionList.add(
                new BasicInstruction("swap $t1,$t2",
            	 "Swap: swap the values of $t1 and $t2",
                BasicInstructionFormat.R_FORMAT,
                "000000 fffff sssss 00000 00000 111000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int add1 = RegisterFile.getValue(operands[0]);
                     int add2 = RegisterFile.getValue(operands[1]);
                     int temp = 0;
                     temp = add1;
                     add1 = add2;
                     add2 = temp;
            
                     RegisterFile.updateRegister(operands[0], add1);
                     RegisterFile.updateRegister(operands[1], add2);
                  }
               }));
         instructionList.add(
                new BasicInstruction("rub $t1",
            	 "Rubbish: put a random value from 1 to 100 into $t1",
                BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 fffff 00000 111001",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     Random random = new Random();
                     int randomNum = random.nextInt(100) + 1;

                     int[] operands = statement.getOperands();
                     RegisterFile.updateRegister(operands[0], randomNum);
                  }
               }));
         instructionList.add(
                new BasicInstruction("syscall", 
            	 "Issue a system call : Execute the system call specified by value in $v0",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 001100",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     Globals.instructionSet.findAndSimulateSyscall(RegisterFile.getValue(2),statement);
                  }
               }));


        
        
    }
}