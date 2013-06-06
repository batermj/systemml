package com.ibm.bi.dml.runtime.controlprogram;

import java.util.ArrayList;
import java.util.HashMap;

import com.ibm.bi.dml.parser.DMLProgram;
import com.ibm.bi.dml.utils.DMLRuntimeException;
import com.ibm.bi.dml.utils.DMLUnsupportedOperationException;


public class Program {
	public static final String KEY_DELIM = "::";
	
	public ArrayList<ProgramBlock> _programBlocks;

	protected LocalVariableMap _programVariables;
	private HashMap<String, HashMap<String,FunctionProgramBlock>> _namespaceFunctions;

		
	public Program() throws DMLRuntimeException {
		_namespaceFunctions = new HashMap<String, HashMap<String,FunctionProgramBlock>>(); 
		_programBlocks = new ArrayList<ProgramBlock>();
		_programVariables = new LocalVariableMap ();
	}

	public void addFunctionProgramBlock(String namespace, String fname, FunctionProgramBlock fpb){
		
		if (namespace == null) 
			namespace = DMLProgram.DEFAULT_NAMESPACE;
		
		HashMap<String,FunctionProgramBlock> namespaceBlocks = null;
		
		synchronized( _namespaceFunctions )
		{
			namespaceBlocks = _namespaceFunctions.get(namespace);
			if (namespaceBlocks == null){
				namespaceBlocks = new HashMap<String,FunctionProgramBlock>();
				_namespaceFunctions.put(namespace,namespaceBlocks);
			}
		}
		
		namespaceBlocks.put(fname,fpb);
	}
	
	public HashMap<String,FunctionProgramBlock> getFunctionProgramBlocks(){
		
		HashMap<String,FunctionProgramBlock> retVal = new HashMap<String,FunctionProgramBlock>();
		
		synchronized( _namespaceFunctions )
		{
			for (String namespace : _namespaceFunctions.keySet()){
				HashMap<String,FunctionProgramBlock> namespaceFSB = _namespaceFunctions.get(namespace);
				for (String fname : namespaceFSB.keySet()){
					retVal.put(namespace+KEY_DELIM+fname, namespaceFSB.get(fname));
				}
			}
		}
		
		return retVal;
	}
	
	public FunctionProgramBlock getFunctionProgramBlock(String namespace, String fname) throws DMLRuntimeException{
		
		if (namespace == null) namespace = DMLProgram.DEFAULT_NAMESPACE;
		
		HashMap<String,FunctionProgramBlock> namespaceFunctBlocks = _namespaceFunctions.get(namespace);
		if (namespaceFunctBlocks == null)
			throw new DMLRuntimeException("namespace " + namespace + " is undefined");
		FunctionProgramBlock retVal = namespaceFunctBlocks.get(fname);
		if (retVal == null)
			throw new DMLRuntimeException("function " + fname + " is undefined in namespace " + namespace);
		//retVal._variables = new LocalVariableMap();
		return retVal;
	}
	
	public void addProgramBlock(ProgramBlock pb) {
		_programBlocks.add(pb);
	}

	public ArrayList<ProgramBlock> getProgramBlocks() {
		return _programBlocks;
	}

	public void execute(LocalVariableMap varMap, ExecutionContext ec)
	throws DMLRuntimeException, DMLUnsupportedOperationException
	{
		//populate initial symbol table
		//_programVariables.putAll (varMap);
		
		SymbolTable symb = createSymbolTable();
		symb.copy_variableMap(varMap);
		
		for (int i=0; i<_programBlocks.size(); i++)
		{
			
			// execute each top-level program block
			ProgramBlock pb = _programBlocks.get(i);

			//pb.setVariables(_programVariables);
			SymbolTable childSymb = symb.getChildTable(i);
			childSymb.copy_variableMap(symb.get_variableMap());
			ec.setSymbolTable(childSymb);
			
			try {
				pb.execute(ec);
			}
			catch(Exception e){
				throw new DMLRuntimeException(pb.printBlockErrorLocation(), e);
			}
			
			//_programVariables = pb.getVariables();
			symb.set_variableMap( ec.getSymbolTable().get_variableMap() );
			ec.setSymbolTable(symb);
		}
	}
	
	public SymbolTable createSymbolTable() {
		SymbolTable st = new SymbolTable(true);
		for (int i=0; i < _programBlocks.size(); i++) {
			st.addChildTable(_programBlocks.get(i).createSymbolTable());
		}
		return st;
	}
	
	/*public void cleanupCachedVariables() throws CacheStatusException
	{
		for( String var : _programVariables.keySet() )
		{
			Data dat = _programVariables.get(var);
			if( dat instanceof MatrixObjectNew )
				((MatrixObjectNew)dat).clearData();
		}
	}*/
	
	public void printMe() {
		
		/*for (String key : _functionProgramBlocks.keySet()) {
			System.out.println("function " + key);
			_functionProgramBlocks.get(key).printMe();
		}*/
		
		for (ProgramBlock pb : this._programBlocks) {
			pb.printMe();
		}
	}
}
