\d .ns

<new-name>:1

{
  if[1;global]
  if[1;global:1]
  if[1;global::1]
  if[1;`<new-name>]
}

if[1;<new-name>]
if[1;<new-name>:1]
if[1;<new-name>::1]
if[1;`<new-name>]

fn:{[global]
  global;
  global:1;
  global::1;
  `<new-name>}

<new-name>
<new-name>:1
<new-name>::1
`<new-name>
