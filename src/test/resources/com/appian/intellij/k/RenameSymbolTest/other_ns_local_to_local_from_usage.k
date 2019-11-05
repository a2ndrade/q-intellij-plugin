\d .a
test:1;

\d .b

sampleFn:{[]
  `.a.<caret>test[2];
  }

.a.test[3]
