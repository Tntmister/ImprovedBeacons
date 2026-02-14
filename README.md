<h1>Improved Beacons</h1>
Adds more layers to beacons, with more effects to choose from, and the possibility to increase its range by using rarer blocks.
<br/><br/>
Requires Fabric API.

<h2>Features</h2>
<ul>
  <li>Increased maximum layers to 6</li>
  <ul>
    <li>Unlocks Luck effect at layer 5</li>
    <li>Unlocks Health Boost effect at layer 6</li>
  </ul>
  <li>Increased range based on composition of pyramid (up to 16 chunks)</li>
  <ul>
    <li>Radius is __(1 + 3 * power) * (layers * 8 + 16)__, where power is the average power of the pyramid (iron = 0, gold = 37, diamond/emerald = 50, netherite = 100)</li>
    <li>For example, a pyramid made entirely out of iron blocks will have power 0, a pyramid made out of 50% iron and 50% netherite will have power 50, and a pyramid made entirely out of netherite will have power 100.</li>
  </ul>
</ul>

<h2>Mod Conflicts</h2>
This mod is probably incompatible with mods that also modify the Beacon.
