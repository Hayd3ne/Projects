import sys
import json
from sentence_transformers import SentenceTransformer

model = SentenceTransformer('all-mpnet-base-v2')

if __name__ == "__main__":
    input_path = sys.argv[1]
    output_path = sys.argv[2]

    # Read input file (list of texts)
    with open(input_path, "r", encoding="utf-8") as f:
        texts = json.load(f)

    # Get embeddings
    embeddings = model.encode(texts, convert_to_numpy=True).tolist()

    # Write output file (list of embeddings)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(embeddings, f)
