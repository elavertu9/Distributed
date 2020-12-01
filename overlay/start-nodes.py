import os, sys
import numpy as np

jar_path='/s/bach/e/under/elavertu/classes/overlay/build/libs/cs455-1.0-SNAPSHOT.jar'
user='elavertu'
domain='cs.colostate.edu'
registry_host=''
registry_port='8099'
machine_list='./machines_list'

def startMessagingNode(mac):
	ssh_cmd = "\" echo 'Connecting to: "+mac+"' && ssh -t "+ \
				user+"@"+mac+"."+domain+" 'hostnamectl && java -cp "+jar_path+ \
				" cs455.overlay.node.MessagingNode "+registry_host+" "+registry_port+"'\""
	command = "gnome-terminal -x bash -c "+ssh_cmd
	print(command)
	os.system(command)


'''
import matplotlib.pyplot as plt
plt.xticks(rotation=80)
plt.plot(machines, node_distribution, 'o--')
plt.show();
'''

if __name__ == "__main__":
	registry_host = sys.argv[1]
	num_nodes = int(sys.argv[2])
	print("Registry Host:", registry_host)

	machines = []
	with open(machine_list) as f:
		for line in f:
			machines += [line.split('\n')[0]]

	machines = np.array(machines)
	node_distribution = [0]*len(machines)
	node_distribution = np.array(node_distribution)

	for i in range(num_nodes):
		machine = np.random.choice(machines)
		startMessagingNode(machine)
		node_distribution[np.argwhere(machines == machine)[0][0]] += 1
