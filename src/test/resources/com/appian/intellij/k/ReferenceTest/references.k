noNs:{x+x}

\d .a.b

implicitNs:{x+x}

fn1:{[]
  inside.fn.relative:1
}

fn2:{[]
  .inside.fn.absolute:2
}

q).a.b.explicitNs:{x+x}

.x._otherExplicitNs:{x+x}

\d .c

fn3:{[]
  globalAmend::3
}

\d .x

otherImplicitNs:{x+x}

.explicitRootNs:{x+x}

\d .

implicitRootNs:{x+x}

\d ^

k)chained:otherNoNs:{x+x}

\d .empty


