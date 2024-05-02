import random
import requests
import sys
import time

# "http://localhost:9090"

def main():

    if len(sys.argv) > 1:
        URL = sys.argv[1]
        print(f"Sending logs to '{URL}'")
    else: 
        exit()

    i = 0
    while(True):
        waitTime = random.randint(1,5)
        rand = random.randint(0,1)

        

        with open(f"logs/log_kart-{i}.txt", "w") as logFile:
            logFile.write("Example log line\n")
            if rand == 1:
                logFile.write(f"kart-{i}:OK")
            else: 
                logFile.write(f"kart-{i}:SCRAP")

        print(f"Created logFile 'log_kart-{i}.txt'!")

        with open(f"logs/log_kart-{i}.txt", "r") as logFile:
            lines = logFile.readlines()
            last_line = lines[-1] if lines else None
            fields = last_line.split(':')

        kart = {
            'id': fields[0],
            'status': fields[1]
        }

        response = requests.post(URL, json=kart)
        print(f"Sent file to '{URL}', RESPONSE = {response}")        

        i += 1
        time.sleep(waitTime)

if __name__ == "__main__":
    main()

    