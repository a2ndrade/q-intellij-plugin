\d .ns

global:1

{
  if[1;global]
  if[1;`global]
}

if[1;global]
if[1;`global]

fnA:{[<new-name>]
  <new-name>;
  `global}

fnB:{[]
  global;
  `global}

global
`global
