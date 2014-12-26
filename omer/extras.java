	private Type getStaticMethodType(StaticCall call) {
		Object c = call.scope.retrieveIdentifier(call.getClassName());
		Method method = ((ICClass) c).scope.getMethod(call.getName());
		return method.getType();
	}

private Type getVirtualMethodType(StaticCall call) {
		if (call.getLocation() == null) {
			m = call.scope.retrieveIdentifier(call.getName());
		} else {
			Type class_type = (Type) call.getLocation().accept(this);
			Object c = call.scope.retrieveIdentifier(class_type.getName());
			m = ((ICClass) c).scope.retrieveIdentifier(call.getName());
		}
		Object c = call.scope.retrieveIdentifier(call.getClassName());
		Method method = ((ICClass) c).scope.getMethod(call.getName());
		return method.getType();
	}
