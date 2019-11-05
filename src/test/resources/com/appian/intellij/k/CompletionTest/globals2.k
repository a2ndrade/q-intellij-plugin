someFnInDefaultNsInAnotherFile:{x+x}

fn1:{[a;b]
  someLocallyDefinedGlobal::a+b;
  :someLocallyDefinedGlobal}

fn2:{[a;b]
  someLocal:a+b;
  :someLocal}

fn3:{[a;b]
  some.locallyDefined.namespacedGlobal:a+b;
  :some.locallyDefined.namespacedGlobal}
