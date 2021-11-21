# Bomberman 
Az eredeti bomberman játék alapkoncepcióját utánzó programról van szó. A játékos egy véletlenszerűen generált pályán találja magát, ahol ellenségek és elpusztítható dobozok és falak találhatók. Lehet bombát is lehelyezni, ami pár másodperc után felrobban tüzet hagyva maga után. A tűz minden vele érintkező játékost, ellenséget és pusztítható dobozt elpusztít. A játék online játszható, így elérhető szervez üzemmódja is a programnak, amire több kliens is fel tud csatlakozni. Ehhez command line kell megadnunk a szerver paramétereit és a szerver elindul GUI megjelenítése nélkül. Lokális játékra is van lehetőség, ilyenkor command line jelezni kell a kliens és szerver együttes működését. Alapértelmezett indításba kliensként viselkedi. Ha meghalunk akkor lehetőség van újra visszacsatlakozni a játékba és folytatni ahol abbahagytuk. Egy beállítási fájl alapján testreszabhajtuk a program számos elemét.

# Use case
## Server mód
`--server` és `--server-port %d` flagek megadásával indítható el a megadott porton. `--client` nélkül console modeban fut.

## Client mód
Egy GUI fogad minket ilyenkor, amit a felső menubarból tudunk vezérelni. A beállítások alatt lehetőségünk van megtekinteni a beállításainkat, illetve megváltoztatni a játékos nevünket. A játék menüpont alatt fel és le tudunk csatlakozni egy szerverről és be tudjuk állítani, hogy a játék vége esetén újracsatlakozzunk-e.

## Vegyes mód
Server és kliens mód kombinációja. Ilyenkor portként ezt használjuk: `127.0.0.1`

## Játék
Játék során WASD gombokkal tudunk mozogni, illetve spaceszel tudunk bombát lehelyezni. Ahhoz hogy nyerjünk el kell pusztítanunk minden ellenséget, illetve meg kell találnunk az egyik doboz alá elrejtett kijáratot, melybe bele kell mennünk hogy nyerjünk.

# Megoldási ötlet
Alapvetően lesz 2 osztály, mely a futási módokat hivatott kezelni: `Server`, `Client`. Ezeket hivatott összekötni egy `Connect` és `Listen` osztály mely az előbbi osztályokhoz kapcsolódik a leírt sorrendben. A játék következő állapotát egy `Tick` nevű osztály kezeli, ami delegálja a egyes objektumok teendőit az adott objektumokra (pl mozgás, eltávolítás előtti cselekedet). Client oldalon lesz egy `Draw` osztály ami a pályát kirajzolja annak modellje alapján, illetve egy `GUI` osztály mely a Swing keretrendszer kezeléséért felelős.

## Server
Alapvetően megadja `Listen`-nek, hogy mit végezzen el amikor egy új játékos csatlakozik, illetve letárolja a jelenleg csatlakozott játákosokat, hogy amikor egy állapot frissítést kap egy klienstől tudja adott játékoshoz kapcsolni.

## Client
Hasonlóan a szerverhez megadja `Connect`-nek, hogy mit csináljon csatlakozáskor (pl. elfogad a szervertől egy auth tokent, amit letárol) és mit csináljon a fogadott új szerver állapotkor (rajzoltassa ki `Draw`-val).

## Connect, Listen
Alapvetően a beépített `Socket`-et használva küld serializált objektumokat. A szerver esetében minden klienst értesít az új állapotáról.

## Tick
Heterogén kollekciót tartalmaz, melyben a pálya elemei lesznek. Ezekre fogja meg fix intervallumonként meghívni, hogy lépjenek, illetve hogy el kell-e pusztulniuk és ha igen kiveszi őket a pályáról.

## Draw
Swing `Graphics`-ot fogja használni képek és player nevek kirajzolásra.

## GUI
Swing componenseket fog használni. Például `JFrame`, `JPanel`, `JMenu`.