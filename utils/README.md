# TD2 Oiseaux : Utility programs
## IOC Decoder
### Description
This program uses the [IOC World Bird List - Master list](https://www.worldbirdnames.org/ioc-lists/master-list-2/) (converted to CSV format) and the [IOC World Bird List - Multilingual list](https://www.worldbirdnames.org/ioc-lists/master-list-2/) (converted to CSV format) to gather all possible information about bird species and generate a database collection.

### Requirements :
- Python 3+

### Run
- Download the [IOC World Bird List - Master list](https://www.worldbirdnames.org/ioc-lists/master-list-2/).
- Convert the Master list to CSV format using an office suite and rename it to `ioc.csv`.
- Download the [IOC World Bird List - Multilingual list](https://www.worldbirdnames.org/ioc-lists/master-list-2/).
- Convert the Master list to CSV format using an office suite and rename it to `ioc-multilingual.csv`.
- Run the program :
```bash
python3 ioc-decoder.py
```
- The database collection will be store in a file named `species.json`
