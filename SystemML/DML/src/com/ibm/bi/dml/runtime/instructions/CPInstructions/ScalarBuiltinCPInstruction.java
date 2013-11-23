/**
 * IBM Confidential
 * OCO Source Materials
 * (C) Copyright IBM Corp. 2010, 2014
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.ibm.bi.dml.runtime.instructions.CPInstructions;

import com.ibm.bi.dml.runtime.DMLRuntimeException;
import com.ibm.bi.dml.runtime.controlprogram.ExecutionContext;
import com.ibm.bi.dml.runtime.instructions.InstructionUtils;
import com.ibm.bi.dml.runtime.matrix.operators.Operator;
import com.ibm.bi.dml.runtime.matrix.operators.SimpleOperator;


public class ScalarBuiltinCPInstruction extends BuiltinUnaryCPInstruction
{	
	@SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp. 2010, 2013\n" +
                                             "US Government Users Restricted Rights - Use, duplication  disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";
	
	public ScalarBuiltinCPInstruction(Operator op, CPOperand in, CPOperand out, String instr)
	{
		super(op, in, out, 1, instr);
	}
	
	@Override 
	public void processInstruction(ExecutionContext ec) 
		throws DMLRuntimeException 
	{	
		String opcode = InstructionUtils.getOpCode(instString);
		SimpleOperator dop = (SimpleOperator) optr;
		ScalarObject sores = null;
		ScalarObject so = null;
		
		//get the scalar input 
		so = ec.getScalarInput( input1.get_name(), input1.get_valueType(), input1.isLiteral() );
			
		//core execution
		if ( opcode.equalsIgnoreCase("print") ) {
			String outString = "";
			outString = so.getStringValue();
			System.out.println(outString);
			// String that is printed on stdout will be inserted into symbol table (dummy, not necessary!) 
			sores = new StringObject(outString);
		}
		else if (opcode.equalsIgnoreCase("print2")) {
			System.out.println(so.getStringValue());
			// String that is printed on stdout will be inserted into symbol table (dummy, not necessary!) 
			sores = new StringObject(so.getStringValue());
		}
		else {
			//Inputs for all builtins other than PRINT are treated as DOUBLE.
			double rval;
			rval = dop.fn.execute(so.getDoubleValue());
			sores = (ScalarObject) new DoubleObject(rval);
		}
		
		ec.setScalarOutput(output.get_name(), sores);
	}

}
