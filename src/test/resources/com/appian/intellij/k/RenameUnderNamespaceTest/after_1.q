\d .ns

<new-name>:1

{
  if[1;<new-name>]
  if[1;`<new-name>]
}

if[1;<new-name>]
if[1;`<new-name>]

fnA:{[global]
  global;
  `<new-name>}

fnB:{[]
  <new-name>;
  `<new-name>}

<new-name>
`<new-name>
