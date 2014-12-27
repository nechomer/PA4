	@Override
	public Object visit(StaticCall call) {
		String lir = "";
		int startMax = currReg;

		String resReg = getNextReg();
		if ( call.getMethod.getType().getName().equals("void") )
			resReg = "Rdummy";
		
		// R_T+1, R_T+2, ...   <--   evaluate arguments
		List<String> paramRegs = new ArrayList<>(call.getArguments().size());
		for (Expression argument : call.getArguments()) {
			currReg++;
			paramRegs.add(getNextReg());
			lir += argument.accept(this);
		}
			
		// R_T <- call the method
		if ( call.getClassName().equals("Library") ) {
			lir += "Library __" + call.getName() + "(";
			for ( int i = 0; i < paramRegs.size()-1; i++ )
				lir += paramRegs.get(i) + ",";
			if ( paramRegs.size() > 0 )
				lir += paramRegs.get(paramRegs.size()-1);
		} else {
			lir += "StaticCall _" + call.getClassName() + "_" + call.getName() + "(";
			lir += getCallArgsStr(call, paramRegs);
		}

		lir += "), " + resReg + "\n";
			
		currReg = startMax;
		return lir;
	}

	@Override
	public Object visit(VirtualCall call) {
		String lir = "";
		int startMax = currReg;
		
		String resReg = getNextReg();
		if ( call.getMethod.getType().getName().equals("void") )
			resReg = "Rdummy";
		
		// allocate object register and evaluate its value
		currReg++;
		String objReg = getNextReg();
		if ( call.isExternal() ) {
			lir += call.getLocation().accept(this);
			lir += nullPtrCheckStr(objReg);
		} else {
			lir += "Move this, " + objReg + "\n";
		}
		
		// calculate method offset
		String className = call.getMethod.scope.getClassOfScope();
		int methodOffset = DispatchTableBuilder.getMethodOffset(className, call.getName());
		
		// R_T+2, R_T+3, ...   <--   evaluate arguments
		List<String> paramRegs = new ArrayList<>(call.getArguments().size());
		for (Expression argument : call.getArguments()) {
			currReg++;
			paramRegs.add(getNextReg());
			lir += argument.accept(this);
		}
			
		// R_T <- call the method
		lir += "VirtualCall " + objReg + "." + methodOffset;
		lir += "(" + getCallArgsStr(call, paramRegs) + "), " + resReg + "\n";
		
		// pop used registers
		currReg = startMax;
		
		return lir;
	}

	private String getCallArgsStr(Call call, List<String> paramRegs) {
		List<Formal> fl = call.getMethod.getFormals();
		for ( int i = 0; i < fl.size() ; i++ ) {
			lir += fl.get(i).getName() + "=" + paramRegs.get(i) + ((i+1==fl.size()) ? "" : ", ");
		}
		return lir;
	}