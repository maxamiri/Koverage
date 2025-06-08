# Koverage

**Koverage** is a Kotlin-based simulator for modelling and evaluating network coverage in industrial environments using mobile sinks, such as service vehicles, autonomous ground vehicles or aerial drones.

It supports a modular architecture to simulate various mobility behaviours and analyse wireless sensor network (WSN) coverage over time.

---

## 🚧 Project Status

This repository currently serves as a placeholder.

🔒 The full source code will be published here after the acceptance of the accompanying research paper. It will include complete Kotlin-based modules, sample scenarios, and logs.

---

## 🚗 Supported Mobility Models

Koverage currently supports three mobility models:

- **Random Waypoint** [Bettstetter, 2001]
- **Random Direction** [Royer, 2001]
- **Specialised Synthetic Model** [Amiri et al., 2024] 

📌 The source code for the *Specialised Synthetic Model* is included and was developed by the same team.
*Specialised Synthetic Model* is a custom mobility model based on real-world trace data, designed to reflect structured vehicular motion in constrained environments such as industrial yards or logistics zones.

This model was presented at the 2024 International Telecommunication Networks and Applications Conference (ITNAC):

```bibtex
@INPROCEEDINGS{10815387,
  author={Amiri, Max and Eyers, David and Huang, Zhiyi},
  booktitle={2024 34th International Telecommunication Networks and Applications Conference (ITNAC)}, 
  title={A Specialised Synthetic Mobility Model Based on Real-World Traces}, 
  year={2024},
  pages={1--5},
  doi={10.1109/ITNAC62915.2024.10815387}
}
```

## 📄 Citation (To Be Added)

Once the paper is published, a BibTeX citation entry will be added here for referencing the research.

---

## 📬 Contact

For questions or collaboration opportunities, feel free to open an issue or reach out once the repository is fully open.

---

> Made with Kotlin. Designed for research.
