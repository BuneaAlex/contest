Lab project done in teams of 2, where we needed to implement a client-server project with a focus on parallel programming. The system utilizes a producer-consumer design pattern, employing thread pools for efficient concurrency management. Specifically, I integrated a thread pool mechanism to handle both client and server tasks concurrently. Additionally, I leveraged the use of futures to facilitate asynchronous processing, enhancing the overall responsiveness and performance of the system.
This is the task to solve:
Each country (client) sends its competitors' results to the organizer (server), which we consider to be present in files. The submission is done in batches of 20 pairs (competitor ID, score) at intervals of Δx (to simulate user action on the interface).

The server retrieves data from clients and adds the country ID to the pairs sent by clients, creating triplets (country ID, competitor ID, score) and adds them to a blocking queue.

The final list is updated with the corresponding operations: it is checked whether a pair with an ID equal to ID_n already exists in the list:

If it exists:

If Score_n is positive, the Score_n is added to the existing score in the node.

If Score_n = -1, then the found node is removed from the list.

If a pair with the same ID is subsequently read, it will not be added to the list.

If it does not exist, a new node with the value (ID_n, Score_n) is added.
The list does not need to be sorted after each insertion; sorting at the end is sufficient.

The server uses p_r threads to fetch this data from clients (a thread pool with p_r threads) and p_w threads that add to the global list of competitors.

After sending the appropriate data for each problem, each client (country) sends a request for information regarding the ranking of countries.

The score of a country is equal to the sum of the scores of all competitors from that country.

Upon receiving such a request, the server (will create a future) will start calculating this ranking beforehand, and when finished, it will send the response to the client. If the server has the country ranking calculated at a time interval smaller than a given Δt, then it does not recalculate and sends that ranking – these calculations are done in the main thread corresponding to the server.

In the end, each client sends a request to receive the final result.

After finalizing the final ranking, the server saves the final ranking on competitors in a file and in another file the ranking by countries.

Then it sends these files to clients as a response to the clients' last request.